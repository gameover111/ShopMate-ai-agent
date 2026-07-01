package com.hsc.haiagent.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class ShopMateAppVectorStoreConfig {

    // 💡 1. 这里的 loader、splitter、enricher 统统不需要了，可以删掉
    // @Resource private ShopMateAppDocumentLoader shopMateAppDocumentLoader;
    // @Resource private MyTokenTextSplitter myTokenTextSplitter;
    // @Resource private MyKeywordEnricher myKeywordEnricher;

    @Bean
    @Primary
    public VectorStore shopMateAppVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 💡 2. 这里只纯粹构建客户端组件，启动速度极快，且绝对不会往数据库塞任何数据！
        return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
//                .dimensions(1024)//阿里
                .dimensions(1536)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(false)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10)
                .build();
    }
}














/*
package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class ShopMateAppVectorStoreConfig {

    @Resource
    private ShopMateAppDocumentLoader shopMateAppDocumentLoader;
    // 1. 引入你的切分器组件
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    // 2. 引入你的关键词增强组件
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
*/
/*    // 配置 ShopMateApp 的向量存储-使用 SimpleVectorStore简单实现
    @Bean
    public VectorStore shopMateAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documents = shopMateAppDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }*//*

    @Bean
    @Primary
    public VectorStore shopMateAppVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(false)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10)
                .build();
        // 【Extract】1. 加载原始 Markdown 文档
        List<Document> rawDocuments = shopMateAppDocumentLoader.loadMarkdowns();

        // 【Transform 1】2. 核心修改：将长文档自主切分成 200 Token 的小数据块
        // 这一步能有效防止后续大模型处理或向量化时单文本超长的问题
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(rawDocuments);

        // 【Transform 2】3. 核心修改：调用 DashScope 大模型自动为每个切片补充 5 个关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);

        // 【Load】4. 批量投喂给 vectorStore 写入 PostgreSQL
        // 注意：这里遍历的目标换成了处理完的 enrichedDocuments
        int batchSize = 10;
        for (int i = 0; i < enrichedDocuments.size(); i += batchSize) {
            List<Document> batch = enrichedDocuments.subList(i, Math.min(i + batchSize, enrichedDocuments.size()));
            // 每次只提交 10 条，由于前面注入关键词已经消耗了时间，这里的 batch 能完美保护 DashScope 向量接口不超限
            vectorStore.add(batch);
        }
        return vectorStore;
    }
}
*/
