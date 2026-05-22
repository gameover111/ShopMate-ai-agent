package com.hsc.haiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShopMateAppDocumentLoaderTest {
    @Resource
    private ShopMateAppDocumentLoader shopMateAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        shopMateAppDocumentLoader.loadMarkdowns();
    }
}