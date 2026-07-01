package com.hsc.haiagent.config;

import com.hsc.haiagent.agent.HManus;
import com.hsc.haiagent.agent.ShopMateAgent;
import com.hsc.haiagent.harness.AgentHarness;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;

/**
 * Harness 初始化配置 — 将 Agent 注册到 Harness。
 */
@Configuration
public class HarnessConfig {

    @Resource
    private AgentHarness agentHarness;

    @Resource
    private HManus hManus;

    @Resource
    private ShopMateAgent shopMateAgent;

    @PostConstruct
    public void registerAgents() {
        agentHarness.registerAgent(hManus);
        agentHarness.registerAgent(shopMateAgent);
        // 编排器依赖已注册的子 Agent 列表来构建提示词
        agentHarness.initializeOrchestrator();
    }
}
