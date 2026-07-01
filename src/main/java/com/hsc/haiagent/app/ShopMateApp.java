package com.hsc.haiagent.app;

import com.hsc.haiagent.advisor.MyLoggerAdvisor;
import com.hsc.haiagent.advisor.PermissionAdvisor;
import com.hsc.haiagent.advisor.SensitiveWordAdvisor;
import com.hsc.haiagent.rag.QueryTransformer;
import com.hsc.haiagent.rag.ShopMateAppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ShopMateApp {
    private final ChatClient chatClient;

    @Resource
    private VectorStore shopMateAppVectorStore;
//    @Resource
//    private Advisor shopMateAppRagCloudAdvisor;

    private static final String SYSTEM_PROMPT = "扮演深耕电商客服沟通领域的专家——店小二。开场向用户表明身份，告知用户可倾诉客服回复中的难题。\n" +
            "围绕售前咨询、售后纠纷、差评投诉三种状态提问：\n" +
            "- 售前咨询状态：询问如何应对顾客比价、产品细节追问、犹豫不决等情况；\n" +
            "- 售后纠纷状态：询问商品质量问题、物流延迟、少发错发引发的矛盾；\n" +
            "- 差评投诉状态：询问如何安抚愤怒顾客、解释规则、争取改评价或避免升级。\n" +
            "引导用户详述对话经过、顾客的原话及情绪反应、以及用户自己打算怎么回复或已经回复的内容，以便给出专属的优化话术。";

    /**
     * 初始化 ai客户端
     * @param chatModel
     */

    public ShopMateApp(ChatModel chatModel, ChatMemory chatMemory) {
        // 使用共享的 ChatMemory Bean（JDBC 持久化）


        // 2.创建 ChatClient 实例
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        //自定义日志记录器advisor，可按需开启
                        new MyLoggerAdvisor(),
                        //权限校验advisor，可按需开启
                        new PermissionAdvisor(),
                        //敏感词校验advisor，可按需开启
                        new SensitiveWordAdvisor()
//                        //自定义重复阅读advisor，可按需开启
//                        ,new ReReadingAdvisor()

                )
                .build();
    }

    private static String resolveChatId(String chatId) {
        return (chatId == null || chatId.isBlank())
                ? "anon-" + java.util.UUID.randomUUID().toString().replace("-", "")
                : chatId;
    }

    /**
     * AI 增强对话（同步）— 整合 RAG 知识库 + 工具调用 + 多轮记忆
     */
    public String doChat(String message, String chatId) {
        String finalChatId = resolveChatId(chatId);

        // 查询翻译（中→英）
        message = queryTransformer.transform(message);

        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param("chat_memory_conversation_id", finalChatId)
                        .param("chat_memory_retrieve_size", 10)
                        .param("user_permissions", Set.of("AI_CHAT", "user"))
                        // RAG 检索增强
                        .advisors(ShopMateAppRagCustomAdvisorFactory
                                .createShopMateAppRagCustomAdvisor(shopMateAppVectorStore))
                )
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    /**
     * AI 增强对话（流式输出）— 整合 RAG 知识库 + 工具调用 + 多轮记忆
     * @param message 用户消息
     * @param chatId 会话id
     * @return 流式输出的客服回复
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        String finalChatId = resolveChatId(chatId);

        // 1. 查询翻译（中→英，提升 RAG 检索效果）
//        message = queryTransformer.transform(message);

        final String finalMessage = message;
        return chatClient
                .prompt()
                .user(finalMessage)
                .advisors(spec -> spec
                        .param("chat_memory_conversation_id", finalChatId)
                        .param("chat_memory_retrieve_size", 10)
                        .param("user_permissions", Set.of("AI_CHAT", "user"))
                        // 2. RAG 检索增强：从向量数据库检索相关文档
                        .advisors(ShopMateAppRagCustomAdvisorFactory
                                .createShopMateAppRagCustomAdvisor(shopMateAppVectorStore))
                )
                // 3. 注册所有工具（搜索、文件、PDF等）
                .toolCallbacks(allTools)
                .stream()
                .content();
    }
    public ShopMateReport doChatWithReport(String message, String chatId) {
        String cid = resolveChatId(chatId);
        ShopMateReport shopMateReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成客服沟通结果，标题为{用户名}的客服沟通报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec
                        .param("chat_memory_conversation_id", cid)
                        .param("chat_memory_retrieve_size", 10)
                        .param("user_permissions", Set.of("AI_CHAT", "user"))

                )
                .call()
                .entity(ShopMateReport.class);
        log.info("shopMateReport: {}", shopMateReport);
        return shopMateReport;
    }

    public String doChatOfImage(String message, String chatId, MultipartFile image) throws IOException {
        String cid = resolveChatId(chatId);
// 1. 构建基础请求
        var promptSpec = chatClient.prompt();

        // 2. 处理用户消息，并添加图片（如果有）
        if (image != null && !image.isEmpty()) {
            promptSpec.user(u -> u
                    .text(message) // 👈 设置文本
                    .media(MimeTypeUtils.IMAGE_PNG, image.getResource()) // 👈 添加图片
            );
        } else {
            promptSpec.user(message); // 纯文本场景，保持简洁
        }

        // 3. 继续链式调用（Advisors 和 call 方法）
        ChatResponse response = promptSpec
                .advisors(spec -> spec
                        .param("chat_memory_conversation_id", cid)
                        .param("chat_memory_retrieve_size", 10)
                        .param("user_permissions", Set.of("AI_CHAT", "user"))
                )
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 电商客服知识库问答功能（支持多轮会话记忆）
     * @param message
     * @param chatId
     * @return
     */
    @Resource
    private QueryTransformer queryTransformer;

    public String doChatWithRag(String message, String chatId) {
        String cid = resolveChatId(chatId);
        // 执行查询重写
//        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        // 💡  查询翻译  核心替换：直接调用基于翻译 API 的转换器，Token 消耗瞬间归零！
        message = queryTransformer.transform(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> {
                    spec.param("chat_memory_conversation_id", cid)
                            .param("chat_memory_retrieve_size", 10)
                            .param("user_permissions", Set.of("AI_CHAT", "user"));
                    //开启日志，便于观察效果
//                    spec.advisors(new MyLoggerAdvisor());
                    //应用rag检索增强服务-向量数据库，知识问答
//                    spec.advisors(QuestionAnswerAdvisor.builder(shopMateAppVectorStore).build());
                    //应用rag检索增强服务（基于阿里云知识库服务）
//                    spec.advisors(shopMateAppRagCloudAdvisor);
                    //应用自定义rag检索增强服务（文档查询器+上下文增强器）
                    spec.advisors(ShopMateAppRagCustomAdvisorFactory
                            .createShopMateAppRagCustomAdvisor(shopMateAppVectorStore));//应用rag知识库问答-向量数据库
                                   })
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // 应用工具

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        String cid = resolveChatId(chatId);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec ->{
                        spec.param("chat_memory_conversation_id", cid)
                                .param("chat_memory_retrieve_size", 10)
                                .param("user_permissions", Set.of("AI_CHAT", "user"));
                    //开启日志，便于观察效果
                    spec.advisors(new MyLoggerAdvisor());
                    //应用rag检索增强服务-向量数据库，知识问答
//                    spec.advisors(QuestionAnswerAdvisor.builder(shopMateAppVectorStore).build());
                    //应用rag检索增强服务（基于阿里云知识库服务）
//                    spec.advisors(shopMateAppRagCloudAdvisor);
                    //应用自定义rag检索增强服务（文档查询器+上下文增强器）
//                    spec.advisors(ShopMateAppRagCustomAdvisorFactory.createShopMateAppRagCustomAdvisor(shopMateAppVectorStore, "品列"));//应用rag知识库问答-向量数据库
                })
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

//    @Resource
//    private ToolCallbackProvider toolCallbackProvider;

  /*  public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec ->{
                    spec.param("chat_memory_conversation_id", chatId)
                            .param("chat_memory_retrieve_size", 10)
                            .param("user_permissions", Set.of("AI_CHAT", "user"));
                    //开启日志，便于观察效果
                    spec.advisors(new MyLoggerAdvisor());
                    //应用rag检索增强服务-向量数据库，知识问答
//                    spec.advisors(QuestionAnswerAdvisor.builder(shopMateAppVectorStore).build());
                    //应用rag检索增强服务（基于阿里云知识库服务）
//                    spec.advisors(shopMateAppRagCloudAdvisor);
                    //应用自定义rag检索增强服务（文档查询器+上下文增强器）
//                    spec.advisors(ShopMateAppRagCustomAdvisorFactory.createShopMateAppRagCustomAdvisor(shopMateAppVectorStore, "品列"));//应用rag知识库问答-向量数据库
                })
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }*/


    record ShopMateReport(String title, List<String> suggestions) {
    }


}
