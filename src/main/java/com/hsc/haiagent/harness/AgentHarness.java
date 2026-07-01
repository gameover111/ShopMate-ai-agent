package com.hsc.haiagent.harness;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AgentHarness — 标准化智能体执行环境。
 * <p>
 * 职责：
 * <ul>
 *   <li>管理执行循环（最大步数、状态流转）</li>
 *   <li>向 Agent 注入 ChatClient 和工具</li>
 *   <li>提供流式输出能力</li>
 *   <li>管理 Agent 注册表</li>
 *   <li>多 Agent 编排路由</li>
 * </ul>
 */
public interface AgentHarness {

    /**
     * 同步执行一个 Agent。
     * @param agent      要执行的 Agent
     * @param userPrompt 用户输入
     * @return 所有步骤拼接的结果文本
     */
    String execute(Agent agent, String userPrompt);

    /**
     * 流式执行一个 Agent，每个步骤结果通过 SSE 推送。
     * @param agent      要执行的 Agent
     * @param userPrompt 用户输入
     * @return SseEmitter（300s 超时）
     */
    SseEmitter executeStream(Agent agent, String userPrompt);

    // ========== Agent 注册表 ==========

    /**
     * 注册一个 Agent 到 Harness。
     */
    void registerAgent(Agent agent);

    /**
     * 按名称获取已注册的 Agent。
     */
    Agent getAgent(String name);

    /**
     * 获取所有已注册的 Agent。
     */
    List<Agent> getAllAgents();

    // ========== 多 Agent 编排 ==========

    /**
     * 通过编排器（MultiAgentOrchestrator）自动判断使用哪个子 Agent，
     * 并以流式输出结果。
     * @param userPrompt 用户输入
     * @return SseEmitter
     */
    SseEmitter orchestrateStream(String userPrompt);
}
