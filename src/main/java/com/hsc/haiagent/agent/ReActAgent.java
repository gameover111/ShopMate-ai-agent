package com.hsc.haiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基于ReAct架构的智能体，定义思考和行动的流程
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {  
  
    /**
     * 思考是否需要行动
     * @return 是否需要行动
     */
    public abstract boolean think();
  
    /**
     * 执行行动
     * @return 行动结果
     */
    public abstract String act();
  
    /**
     * 执行单个步骤
     * @return 执行结果
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