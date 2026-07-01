package com.hsc.haiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.hsc.haiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支持工具调用的 ReAct Agent。
 * <p>
 * think() — 调用 LLM 判断是否需要使用工具<br>
 * act() — 执行 LLM 请求的工具调用<br>
 * <p>
 * 工具由 {@link com.hsc.haiagent.harness.AgentHarness} 通过 {@link #setToolCallbacks} 注入。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    /** 工具调用的聊天响应（think 阶段记录，act 阶段消费） */
    private ChatResponse toolCallChatResponse;

    /** 上一次 think 的纯文本回复（无工具调用时返回给用户） */
    private String lastAssistantText = "";

    @Override
    public void cleanup() {
        this.initialPromptSent = false;
        this.lastAssistantText = "";
    }

    /** 工具调用管理器 */
    private final ToolCallingManager toolCallingManager;

    /** 聊天选项（禁用内部工具执行） */
    private final ChatOptions chatOptions;

    public ToolCallAgent() {
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = OpenAiChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 带初始工具的构造器（用于编排器等特殊 Agent）。
     */
    public ToolCallAgent(ToolCallback[] toolCallbacks) {
        this();
        setToolCallbacks(toolCallbacks);
    }

    /** 记录是否已发过初始提示词，避免每轮重复追加 */
    private boolean initialPromptSent = false;

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                // 返回 LLM 的真实回复文本，而非固定提示
                String text = lastAssistantText;
                lastAssistantText = "";
                return text.isBlank() ? "思考完成" : text;
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }

    @Override
    public boolean think() {
        // 1. 只在第一步发送下一步提示词，之后不再重复追加
        if (!initialPromptSent && getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
            initialPromptSent = true;
        }

        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            // 2. 调用 LLM，传入已注入的工具
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(getToolCallbacks())
                    .call()
                    .chatResponse();

            // 记录响应，供 act 阶段使用
            this.toolCallChatResponse = chatResponse;

            // 3. 解析响应
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.info("{} 的思考: {}", getName(), result);
            log.info("{} 选择了 {} 个工具", getName(), toolCallList.size());

            String toolCallInfo = toolCallList.stream()
                    .map(tc -> String.format("工具名称：%s，参数：%s", tc.name(), tc.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            // 没有工具调用 → 记录回复文本，标记完成
            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                this.lastAssistantText = result != null ? result : "";
                setState(AgentState.FINISHED);
                return false;
            }
            // 有工具调用，由 act 处理
            return true;

        } catch (Exception e) {
            log.error("{} 的思考过程遇到了问题: {}", getName(), e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        // 执行工具调用
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 更新消息上下文
        setMessageList(toolExecutionResult.conversationHistory());

        // 解析工具调用结果（友好展示，截取前 200 字符避免刷屏）
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> {
                    String data = response.responseData();
                    if (data != null && data.length() > 200) {
                        data = data.substring(0, 200) + "…";
                    }
                    return "✓ 工具[" + response.name() + "] 执行完成";
                })
                .collect(Collectors.joining("\n"));

        // 判断是否调用了 doTerminate 工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        log.info(results);
        return results;
    }
}
