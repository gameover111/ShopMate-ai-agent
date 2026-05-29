package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShopMateAppDocumentLoaderTest {
    @Resource
    private ShopMateAppDocumentLoader shopMateAppDocumentLoader;
    @Resource
    private ShopMateAppDocumentLoaderWithGitHubRepo shopMateAppDocumentLoaderWithGitHubRepo;

//    @Test
//    void loadMarkdowns() {
//        shopMateAppDocumentLoader.loadMarkdowns();
//    }
    @Test
    void loadGitHubRepoData() {
        List<Document> documents = shopMateAppDocumentLoaderWithGitHubRepo.loadGitHubRepoData();
        assertNotNull(documents);
    }
}