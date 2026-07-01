package com.hsc.haiagent.agent;

import org.springframework.stereotype.Component;

/**
 * HManus — 全能型超级智能体，能自主规划并调用工具完成任务。
 * <p>
 * 构造器只设置名称和提示词。
 * ChatClient 和工具由 {@link com.hsc.haiagent.harness.AgentHarness} 在 execute 前注入。
 */
@Component
public class HManus extends ToolCallAgent {

    public HManus() {
        this.setName("HManus");
        this.setSystemPrompt("""
                You are HManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """);
        this.setNextStepPrompt("""
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """);
        this.setMaxSteps(20);
    }
}
