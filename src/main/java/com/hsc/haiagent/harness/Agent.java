package com.hsc.haiagent.harness;

import com.hsc.haiagent.agent.model.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Agent 接口 — 定义智能体的核心契约。
 * Agent 只关注"做什么"（think/act 逻辑），
 * 执行循环由 {@link AgentHarness} 统一管理。
 */
public interface Agent {

    // ========== 身份 ==========

    String getName();
    void setName(String name);

    // ========== 配置 ==========

    String getSystemPrompt();
    void setSystemPrompt(String systemPrompt);

    String getNextStepPrompt();
    void setNextStepPrompt(String nextStepPrompt);

    int getMaxSteps();
    void setMaxSteps(int maxSteps);

    // ========== 状态 ==========

    AgentState getState();
    void setState(AgentState state);

    // ========== 运行时（由 Harness 注入） ==========

    List<Message> getMessageList();
    void setMessageList(List<Message> messageList);

    ChatClient getChatClient();
    void setChatClient(ChatClient chatClient);

    /**
     * 由 Harness 在 execute 前注入工具列表。
     * 不需要工具的 Agent 可留空实现。
     */
    default void setToolCallbacks(ToolCallback[] toolCallbacks) {
        // 默认空实现
    }

    /**
     * 获取当前已注入的工具列表。
     */
    default ToolCallback[] getToolCallbacks() {
        return new ToolCallback[0];
    }

    // ========== 核心逻辑 ==========

    /**
     * 执行一个步骤（一次 think + act）。
     * @return 步骤执行结果文本
     */
    String step();

    /**
     * 清理资源。Harness 在每次 execute 结束后调用。
     */
    default void cleanup() {
        // 子类可重写
    }
}
