package com.hsc.haiagent.agent;

import com.hsc.haiagent.app.ShopMateApp;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 电商客服 Agent — 包装 {@link ShopMateApp} 使其适配 Harness 架构。
 * <p>
 * 这是一个单步 Agent（maxSteps=1），step() 委托给 ShopMateApp 处理。
 * 注册到 Harness 后，可由 MultiAgentOrchestrator 编排调度。
 */
@Component
public class ShopMateAgent extends BaseAgent {

    private final ShopMateApp shopMateApp;

    public ShopMateAgent(ShopMateApp shopMateApp) {
        this.shopMateApp = shopMateApp;
        this.setName("ShopMate");
        this.setSystemPrompt("""
                扮演深耕电商客服沟通领域的专家——店小二。
                围绕售前咨询、售后纠纷、差评投诉三种状态提供话术优化建议。
                """);
        this.setMaxSteps(1);
    }

    @Override
    public String step() {
        // 取 messageList 中最后一条用户消息
        String userMessage = getMessageList().stream()
                .filter(m -> m instanceof UserMessage)
                .reduce((first, second) -> second)
                .map(m -> ((UserMessage) m).getText())
                .orElse("");

        if (userMessage.isBlank()) {
            return "未找到用户输入";
        }

        // 委托给 ShopMateApp 处理
        return shopMateApp.doChat(userMessage, "sub-" + getName() + "-" + System.currentTimeMillis());
    }
}
