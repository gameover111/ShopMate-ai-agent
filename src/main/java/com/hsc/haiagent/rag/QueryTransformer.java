package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QueryTransformer {

    @Resource
    private TranslationGateway translationGateway;

    /**
     * 核心转换方法
     * @param originalQuery 用户的原始提问（如：我喜欢鞋，给我推荐下）
     * @return 转换/翻译后的查询语句
     */
    public String transform(String originalQuery) {
        log.info("【查询转换前】: {}", originalQuery);
        
        // 💡 核心改动：用速度更快、成本更低的第三方 API 代替大模型
        String transformedQuery = translationGateway.translateToEnglish(originalQuery);
        
        log.info("【查询转换后】: {}", transformedQuery);
        return transformedQuery;
    }
}