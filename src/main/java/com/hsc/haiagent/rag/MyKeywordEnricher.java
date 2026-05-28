package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 AI 的文档元信息增强器（为文档补充元信息）
 */
@Component
public class MyKeywordEnricher {

    private final KeywordMetadataEnricher keywordMetadataEnricher;

    // 通过构造器注入 ChatModel，并在初始化时一次性构建好增强器
    public MyKeywordEnricher(ChatModel dashscopeChatModel) {
        // 传入大模型实例，并指定每个 Document 提取 5 个关键词
        this.keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
    }

    public List<Document> enrichDocuments(List<Document> documents) {
        // 直接复用单例实例，性能更优
        return keywordMetadataEnricher.apply(documents);
    }
}
