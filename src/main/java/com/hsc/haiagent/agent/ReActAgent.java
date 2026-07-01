package com.hsc.haiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基于 ReAct 架构的智能体，定义思考和行动的流程。
 * <p>
 * step() = think() → 如果需要行动 → act()
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 思考是否需要行动。
     * @return true = 需要行动（调用 act），false = 思考完成无需行动
     */
    public abstract boolean think();

    /**
     * 执行行动。
     * @return 行动结果文本
     */
    public abstract String act();

    /**
     * 执行单个步骤：先思考，再决定是否行动。
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
