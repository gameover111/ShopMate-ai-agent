package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest
@Slf4j
class KnowledgeBaseInitTest {

    @Resource
    private ShopMateAppDocumentLoader shopMateAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    @Resource
    private VectorStore shopMateAppVectorStore;

    @Test
    void initKnowledgeBaseOnlyOnce() {
        // 1. 加载
        List<Document> rawDocuments = shopMateAppDocumentLoader.loadMarkdowns();

        // 2. 切分 (日志显示你切分成了 7 个 chunk)
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(rawDocuments);

        // 3. 增强
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);

        // 💡 4. 安全分批机制：设置为 10
        int strictBatchSize = 10;
        for (int i = 0; i < enrichedDocuments.size(); i += strictBatchSize) {
            int end = Math.min(i + strictBatchSize, enrichedDocuments.size());

            // 严格截取子列表
            List<Document> tinyBatch = enrichedDocuments.subList(i, end);

            log.info("【知识库同步】正在提交第 {} 到 {} 条数据（本次提交 {} 条）", i, end, tinyBatch.size());

            // 写入数据库
            shopMateAppVectorStore.add(tinyBatch);

            // 歇一歇，防止并发过高被阿里限流
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        log.info("整个知识库已成功、安全地导入到 PostgreSQL 中！");
    }
}