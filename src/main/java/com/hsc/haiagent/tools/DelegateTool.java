package com.hsc.haiagent.tools;

import com.hsc.haiagent.harness.Agent;
import com.hsc.haiagent.harness.AgentHarness;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 委托工具 — 允许编排器 Agent 将任务委派给注册在 Harness 中的子 Agent。
 * 该工具由 {@link com.hsc.haiagent.harness.DefaultAgentHarness} 创建，
 * 并仅注册到编排器（MultiAgentOrchestrator）。
 */
public class DelegateTool {

    private final AgentHarness harness;

    public DelegateTool(AgentHarness harness) {
        this.harness = harness;
    }

    @Tool(description = """
            Delegate a task to a specific sub-agent. 
            Use this when the user's request matches a specialized agent's capability.
            Returns the full result from the sub-agent.
            """)
    public String delegateToAgent(
            @ToolParam(description = "Name of the target agent to delegate the task to") String agentName,
            @ToolParam(description = "The complete task description to pass to the target agent") String task) {

        Agent agent = harness.getAgent(agentName);
        if (agent == null) {
            return "错误：找不到名为 '" + agentName + "' 的 Agent。可用 Agent: "
                    + harness.getAllAgents().stream().map(Agent::getName).toList();
        }

        return harness.execute(agent, task, null);
    }
}
