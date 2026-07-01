package com.hsc.haiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建上下文查询增强器的工厂
 */
public class ShopMateAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
    }
}
