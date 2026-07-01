package com.hsc.haiagent.agent;

import com.hsc.haiagent.harness.AgentHarness;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HManusTest {

    @Resource
    private AgentHarness agentHarness;

    @Resource
    private HManus hManus;

    @Test
    public void run() {
        String userPrompt = """
                现在在上海静安区，请帮我找到5公里以内的合适运动鞋店铺，图片也生成出来,
                并且结合一下额网络图片，指定一份详细的购物计划，
                并以pdf输出，要用中文格式的
                """;
        String answer = agentHarness.execute(hManus, userPrompt);
        Assertions.assertNotNull(answer);
        System.out.println("=== HManus 执行结果 ===");
        System.out.println(answer);
    }
}
