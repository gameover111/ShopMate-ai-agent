package com.hsc.haiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 */
public class ShopMateAppRagCustomAdvisorFactory {

    /**
     * 创建 RAG 检索增强顾问（不过滤，检索全部知识库文档）
     *
     * @param vectorStore 向量存储
     * @return RAG 检索增强顾问
     */
    public static Advisor createShopMateAppRagCustomAdvisor(VectorStore vectorStore) {
        // 创建文档检索器（不过滤，检索全部文档）
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(3)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(ShopMateAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
