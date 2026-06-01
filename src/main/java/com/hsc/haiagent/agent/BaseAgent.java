package com.hsc.haiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.hsc.haiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体基类，定义基本信息和多步骤执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 智能体名称
    private String name;

    // 系统提示
    private String systemPrompt;
    // 下一步提示
    private String nextStepPrompt;

    // 智能体状态
    private AgentState state = AgentState.IDLE;

    // 最大执行步骤数
    private int maxSteps = 10;
    // 当前执行步骤
    private int currentStep = 0;

    // LLM大模型
    private ChatClient chatClient;

    // 消息列表-memory基于，需要自主维护会话上下文
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行智能体
     * @param userPrompt 用户提示
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        state = AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);

                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }

            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {

            this.cleanup();
        }
    }
    /**
     * 流式输出执行智能体
     * @param userPrompt 用户提示
     * @return 流式输出的智能体回复
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的SSEEmitter
        SseEmitter emitter = new SseEmitter(300000L);
        // 异步执行智能体逻辑
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }


                state = AgentState.RUNNING;

                messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxSteps);


                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;


                        emitter.send(result);
                    }

                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                    }

                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {

                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });


        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }


    /**
     * 定义单个步骤
     * @return 执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        //子类可以重写，清理资源
    }
}
