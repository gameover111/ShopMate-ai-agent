package com.hsc.haiagent.harness;

import com.hsc.haiagent.agent.ToolCallAgent;
import com.hsc.haiagent.tools.DelegateTool;
import org.springframework.ai.support.ToolCallbacks;

/**
 * 多 Agent 编排器 — 一个特殊的 ToolCallAgent。
 * <p>
 * 职责：
 * <ol>
 *   <li>分析用户请求意图</li>
 *   <li>通过 {@link DelegateTool#delegateToAgent} 将任务委派给最合适的子 Agent</li>
 *   <li>将子 Agent 的返回结果整理后回复用户</li>
 * </ol>
 * <p>
 * 编排器只拥有 {@link DelegateTool} 一个工具，防止它绕过委派直接执行。
 */
public class MultiAgentOrchestrator extends ToolCallAgent {

    public MultiAgentOrchestrator(DelegateTool delegateTool, String systemPrompt) {
        // 只注册 DelegateTool — 编排器没有其他工具，无法自己执行任何任务
        super(ToolCallbacks.from(delegateTool));
        this.setName("Orchestrator");
        this.setSystemPrompt(systemPrompt);
        this.setNextStepPrompt("""
                You have zero ability to answer any question or perform any task yourself.
                You MUST call delegateToAgent for EVERY user request.
                After receiving the result from the sub-agent, present it to the user.
                Do NOT generate any content yourself.
                """);
        this.setMaxSteps(5);
    }
}
