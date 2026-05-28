package com.hsc.haiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义基于 Token 的切词器
 */
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import java.util.List;

@Component // 如果你使用 Spring，建议将其作为可复用的 Bean 组件进行管理
public class MyTokenTextSplitter {

    // 线程安全实例，初始化一次，全局复用，避免严重的内存和 CPU 开销
    private final TokenTextSplitter defaultSplitter = new TokenTextSplitter();

    private final TokenTextSplitter customSplitter = TokenTextSplitter.builder()
            .withChunkSize(200)
            .withMinChunkSizeChars(100)
            .withMinChunkLengthToEmbed(10)
            .withMaxNumChunks(5000)
            .withKeepSeparator(true)
            .build();

    public List<Document> splitDocuments(List<Document> documents) {
        return defaultSplitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        return customSplitter.apply(documents);
    }
}