package com.hsc.haiagent.harness;

import cn.hutool.core.util.StrUtil;
import com.hsc.haiagent.advisor.MyLoggerAdvisor;
import com.hsc.haiagent.agent.model.AgentState;
import com.hsc.haiagent.tools.DelegateTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认 AgentHarness 实现 — 只负责执行循环，不管理持久化。
 */
@Slf4j
@Component
public class DefaultAgentHarness implements AgentHarness {

    private final ChatModel chatModel;
    private final ToolCallback[] allTools;

    private final Map<String, Agent> agentRegistry = new ConcurrentHashMap<>();
    private Agent orchestrator;

    public DefaultAgentHarness(ChatModel chatModel, ToolCallback[] allTools) {
        this.chatModel = chatModel;
        this.allTools = allTools;
    }

    @Override
    public String execute(Agent agent, String userPrompt, String chatId) {
        if (agent.getState() != AgentState.IDLE) {
            throw new RuntimeException("无法从状态 " + agent.getState() + " 运行 Agent: " + agent.getName());
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("不能使用空提示词运行 Agent");
        }

        initializeAgent(agent);
        agent.getMessageList().add(new UserMessage(userPrompt));
        agent.setState(AgentState.RUNNING);

        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < agent.getMaxSteps() && agent.getState() != AgentState.FINISHED; i++) {
                log.info("[{}] 执行步骤 {}/{}", agent.getName(), i + 1, agent.getMaxSteps());
                results.add("Step " + (i + 1) + ": " + agent.step());
            }
            if (agent.getState() == AgentState.RUNNING) {
                agent.setState(AgentState.FINISHED);
                results.add("执行结束: 达到最大步骤 (" + agent.getMaxSteps() + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            agent.setState(AgentState.ERROR);
            log.error("[{}] 执行失败", agent.getName(), e);
            return "执行错误: " + e.getMessage();
        } finally {
            resetAgent(agent);
        }
    }

    @Override
    public SseEmitter executeStream(Agent agent, String userPrompt, String chatId, Runnable onBeforeReset) {
        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (agent.getState() != AgentState.IDLE) {
                    emitter.send("错误：无法从状态 " + agent.getState() + " 运行 Agent: " + agent.getName());
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行 Agent");
                    emitter.complete();
                    return;
                }

                initializeAgent(agent);
                agent.getMessageList().add(new UserMessage(userPrompt));
                agent.setState(AgentState.RUNNING);

                // 执行循环，逐步发送中间状态和最终结果
                for (int i = 0; i < agent.getMaxSteps() && agent.getState() != AgentState.FINISHED; i++) {
                    log.info("[{}] 执行步骤 {}/{}", agent.getName(), i + 1, agent.getMaxSteps());
                    String stepResult = agent.step();
                    // 工具执行结果 → 显示友好的状态提示
                    if (stepResult.startsWith("✓")) {
                        String toolName = stepResult.replaceAll("✓ 工具\\[(.*?)\\].*", "$1");
                        emitter.send("🛠 " + toolName);
                    }
                    // 最终回复 → 完整输出（前端 step 模式独立气泡，accumulate 模式自动拼接）
                    else if (!stepResult.equals("思考完成") && !stepResult.equals("没有工具调用")) {
                        emitter.send(stepResult);
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                log.error("[{}] 流式执行失败", agent.getName(), e);
                try {
                    emitter.send("执行错误: " + e.getMessage());
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                if (onBeforeReset != null) {
                    try { onBeforeReset.run(); } catch (Exception ex) {
                        log.warn("[{}] onBeforeReset 回调异常", agent.getName(), ex);
                    }
                }
                resetAgent(agent);
            }
        });

        emitter.onTimeout(() -> {
            resetAgent(agent);
            log.warn("[{}] SSE 连接超时", agent.getName());
        });
        emitter.onCompletion(() ->
                log.info("[{}] SSE 连接完成", agent.getName())
        );

        return emitter;
    }

    @Override
    public void registerAgent(Agent agent) {
        agentRegistry.put(agent.getName(), agent);
    }

    @Override
    public Agent getAgent(String name) {
        return agentRegistry.get(name);
    }

    @Override
    public List<Agent> getAllAgents() {
        return List.copyOf(agentRegistry.values());
    }

    @Override
    public void initializeOrchestrator() {
        ensureOrchestratorInitialized();
    }

    @Override
    public SseEmitter orchestrateStream(String userPrompt, String chatId, Runnable onBeforeReset) {
        ensureOrchestratorInitialized();
        return executeStream(orchestrator, userPrompt, chatId, onBeforeReset);
    }

    private void initializeAgent(Agent agent) {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(agent.getSystemPrompt())
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        agent.setChatClient(chatClient);

        ToolCallback[] existing = agent.getToolCallbacks();
        if (existing == null || existing.length == 0) {
            agent.setToolCallbacks(allTools);
        }
    }

    private void resetAgent(Agent agent) {
        agent.setState(AgentState.IDLE);
        agent.getMessageList().clear();
        agent.cleanup();
    }

    private void ensureOrchestratorInitialized() {
        if (orchestrator != null) return;
        DelegateTool delegateTool = new DelegateTool(this);
        orchestrator = new MultiAgentOrchestrator(delegateTool, buildOrchestratorSystemPrompt());
        registerAgent(orchestrator);
    }

    private String buildOrchestratorSystemPrompt() {
        String agentDescriptions = agentRegistry.entrySet().stream()
                .map(entry -> {
                    Agent a = entry.getValue();
                    return "- " + a.getName() + ": " + describeAgent(a);
                })
                .collect(Collectors.joining("\n"));

        return """
                You are a routing orchestrator. You CANNOT answer any question or perform any task yourself.
                You have only ONE tool: delegateToAgent. You MUST use it for EVERY request.
                
                Available sub-agents:
                %s

                Routing rules:
                1. If the user mentions "shopmate", "店小二", "电商", "客服", "售前", "售后", "差评", "话术" or any e-commerce/customer-service related topic → delegate to ShopMate
                2. For searching, downloading, PDF generation, email sending, file operations, terminal commands, or any task requiring tools → delegate to HManus
                3. If unsure, delegate to HManus
                
                Workflow:
                1. Call delegateToAgent(agentName, task) — pass the FULL user request as the task
                2. Return the result directly to the user without modification
                
                CRITICAL: Never generate any content yourself. Always delegate.
                """.formatted(agentDescriptions);
    }

    private String describeAgent(Agent agent) {
        String prompt = agent.getSystemPrompt();
        if (StrUtil.isNotBlank(prompt)) {
            return prompt.length() > 60 ? prompt.substring(0, 60) + "…" : prompt;
        }
        return agent.getClass().getSimpleName();
    }
}
