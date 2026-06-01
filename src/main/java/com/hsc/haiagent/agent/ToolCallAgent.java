package com.hsc.haiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
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
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data  
@Slf4j  
public class ToolCallAgent extends ReActAgent {  
  
    // 可用的工具
    private final ToolCallback[] availableTools;  
  
    // 工具调用的聊天响应
    private ChatResponse toolCallChatResponse;  
  
    // 工具调用管理器
    private final ToolCallingManager toolCallingManager;  
  
    // 聊天选项
    private final ChatOptions chatOptions;  
  
    public ToolCallAgent(ToolCallback[] availableTools) {  
        super();  
        this.availableTools = availableTools;  
        this.toolCallingManager = ToolCallingManager.builder().build();  
        // 禁用内部工具执行,自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();  
    }

    @Override
    public boolean think() {
        //1.校验提示词，拼接用户提示词
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            //2.调用聊天客户端获取工具调用结果
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //记录响应，用于后续act
            this.toolCallChatResponse = chatResponse;
            //3.解析响应，提取工具调用信息
            //助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取要调用的工具列表
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //如果没有工具调用，直接返回false
            if (toolCallList.isEmpty()) {
                //只有不调用工具时，才需要添加助手消息到消息列表
                getMessageList().add(assistantMessage);
                return false;
            } else {
                //如果有工具调用，无需记录助手消息，因为工具调用会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        //校验是否有工具调用
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        //执行工具调用
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文，conversationHistory包含所有消息（包括上一条用户消息、助手消息、工具调用消息）
        setMessageList(toolExecutionResult.conversationHistory());
        //解析工具调用结果
        //助手消息
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        //判断是否调用了doTerminate工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        //记录工具调用结果
        log.info(results);
        return results;
    }
}


