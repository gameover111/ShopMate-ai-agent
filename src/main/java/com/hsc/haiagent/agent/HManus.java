package com.hsc.haiagent.agent;

import org.springframework.stereotype.Component;

/**
 * HManus — 全能型超级智能体
 */
@Component
public class HManus extends ToolCallAgent {

    public HManus() {
        this.setName("HManus");
        this.setSystemPrompt("""
                You are HManus, an all-capable AI assistant.

                CORE RULES:
                1. Use tools ONLY when the user explicitly asks you to perform an action (search, download, generate PDF, send email, etc.).
                2. For normal conversation, questions, or follow-ups — just answer directly from your knowledge or the conversation history. Do NOT call any tools.
                3. Review the conversation history before acting. If the user is asking about something already done, just remind them.
                4. When you DO use tools, summarize the results in Chinese afterward.
                5. When the task is complete, call the `terminate` tool to end.
                """);
        this.setNextStepPrompt("""
                Review what the user just asked. If it's a simple question or follow-up, answer directly without tools.
                Only use tools if the user explicitly requests a new action.
                """);
        this.setMaxSteps(20);
    }
}
