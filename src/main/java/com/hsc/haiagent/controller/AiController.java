package com.hsc.haiagent.controller;

import com.hsc.haiagent.agent.HManus;
import com.hsc.haiagent.app.ShopMateApp;
import com.hsc.haiagent.harness.Agent;
import com.hsc.haiagent.harness.AgentHarness;
import com.hsc.haiagent.service.ConversationStore;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AgentHarness agentHarness;

    @Resource
    private HManus hManus;

    @Resource
    private ConversationStore conversationStore;

    // ========== HManus 超级智能体 ==========

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message, String chatId) {
        // 1. 加载该会话的历史消息到 Agent 的 messageList
        List<Map<String, String>> history = conversationStore.getMessages(chatId);
        for (Map<String, String> msg : history) {
            String role = msg.get("role");
            String content = msg.get("content");
            if ("user".equals(role) && content != null) {
                hManus.getMessageList().add(new UserMessage(content));
            } else if ("assistant".equals(role) && content != null) {
                hManus.getMessageList().add(new AssistantMessage(content));
            }
        }

        // 2. 执行 Agent（通过 onBeforeReset 在 resetAgent 前保存消息）
        SseEmitter emitter = agentHarness.executeStream(hManus, message, chatId, () -> {
            // 保存用户提问（直接使用原始参数，避免被 nextStepPrompt 覆盖）
            conversationStore.addUserMessage(chatId, message);
            // 保存助手回复（跳过 Step N: / ✓ 等中间步骤）
            List<Message> msgs = hManus.getMessageList();
            for (int i = msgs.size() - 1; i >= 0; i--) {
                Message m = msgs.get(i);
                if (m instanceof AssistantMessage && m.getText() != null && !m.getText().isBlank()
                        && !m.getText().startsWith("Step") && !m.getText().startsWith("✓")) {
                    conversationStore.addAssistantMessage(chatId, m.getText());
                    break;
                }
            }
        });

        return emitter;
    }

    @GetMapping("/manus/sessions/{chatId}/messages")
    public List<Map<String, String>> getManusSessionMessages(@PathVariable String chatId) {
        return conversationStore.getMessages(chatId);
    }

    @GetMapping("/orchestrator/sessions/{chatId}/messages")
    public List<Map<String, String>> getOrchestratorSessionMessages(@PathVariable String chatId) {
        return conversationStore.getOrchestratorMessages(chatId);
    }

    // ========== 多 Agent 编排 ==========

    @GetMapping("/orchestrator/chat")
    public SseEmitter doChatWithOrchestrator(String message, String chatId) {
        // 1. 加载历史到编排器 Agent 的 messageList
        Agent orch = agentHarness.getAgent("Orchestrator");
        if (orch != null) {
            List<Map<String, String>> history = conversationStore.getOrchestratorMessages(chatId);
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                String content = msg.get("content");
                if ("user".equals(role) && content != null) {
                    orch.getMessageList().add(new org.springframework.ai.chat.messages.UserMessage(content));
                } else if ("assistant".equals(role) && content != null) {
                    orch.getMessageList().add(new org.springframework.ai.chat.messages.AssistantMessage(content));
                }
            }
        }

        // 2. 执行编排器（通过 onBeforeReset 在 resetAgent 前保存）
        return agentHarness.orchestrateStream(message, chatId, () -> {
            // 保存用户提问
            conversationStore.addOrchestratorMessage(chatId, "user", message);
            // 保存编排器回复
            if (orch != null) {
                List<Message> msgs = orch.getMessageList();
                for (int i = msgs.size() - 1; i >= 0; i--) {
                    Message m = msgs.get(i);
                    if (m instanceof org.springframework.ai.chat.messages.AssistantMessage
                            && m.getText() != null && !m.getText().isBlank()) {
                        conversationStore.addOrchestratorMessage(chatId, "assistant", m.getText());
                        break;
                    }
                }
            }
        });
    }

    // ========== ShopMateApp 电商客服 ==========

    @Resource
    private ShopMateApp shopMateApp;

    @GetMapping("/shop_mate_app/chat/sync")
    public String dochatWithShopMateAppSync(String message, String chatId) {
        return shopMateApp.doChat(message, chatId);
    }

    @GetMapping(value = "/shop_mate_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> dochatWithShopMateAppSSE(String message, String chatId) {
        return shopMateApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/shop_mate_app/chat/sse")
    public Flux<ServerSentEvent<String>> dochatWithShopMateAppServerSentEvent(String message, String chatId) {
        return shopMateApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/shop_mate_app/chat/sse/emitter")
    public SseEmitter doChatWithShopMateAppSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        shopMateApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try { emitter.send(chunk); }
                            catch (IOException e) { emitter.completeWithError(e); }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }
}
