package com.hsc.haiagent.agent;

import com.hsc.haiagent.agent.model.AgentState;
import com.hsc.haiagent.harness.Agent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体基类 — 实现 {@link Agent} 接口。
 * <p>
 * 只持有身份、状态、配置和运行时上下文，
 * 执行循环由 {@link com.hsc.haiagent.harness.AgentHarness} 统一管理。
 */
@Data
@Slf4j
public abstract class BaseAgent implements Agent {

    // ========== 身份 ==========
    private String name;

    // ========== 配置 ==========
    private String systemPrompt;
    private String nextStepPrompt;
    private int maxSteps = 10;

    // ========== 状态 ==========
    private AgentState state = AgentState.IDLE;

    // ========== 运行时（由 Harness 注入） ==========
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();

    // ========== 工具（由 Harness 注入） ==========
    private ToolCallback[] toolCallbacks;

    // ========== Agent 接口实现 ==========

    @Override
    public abstract String step();

    @Override
    public void cleanup() {
        // 子类可重写
    }
}
