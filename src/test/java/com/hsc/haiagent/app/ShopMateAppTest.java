package com.hsc.haiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
class ShopMateAppTest {

    @Resource
    private ShopMateApp shopMateApp;

    @Test
    void testDoChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好,我是hsc";
        String response = shopMateApp.doChat(message, chatId);
        assertNotNull(response);
        String message2 = "我想让店小二帮我优化我的回复，使回复更符合我的需求";
        response = shopMateApp.doChat(message2, chatId);
        assertNotNull(response);
        String message3 = "你好,我的要求是什么来着，我需要你帮我回忆一下";
        response = shopMateApp.doChat(message3, chatId);
        assertNotNull(response);

    }
    @Test
    void testDoChat2() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好,我是hsc";
        String response = shopMateApp.doChat(message, chatId);
        assertNotNull(response);
        String message2 = "如果用户很弱-智我该怎么办";
        response = shopMateApp.doChat(message2, chatId);
        assertNotNull(response);
    }



    @Test
    void testDoChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好,我是hsc。我想让店小二帮我处理客户质量售后纠纷，我不知道该怎么做";
        ShopMateApp.ShopMateReport shopMateReport = shopMateApp.doChatWithReport(message, chatId);
        assertNotNull(shopMateReport);
    }

    @Test
    void testDoChatOfImage() {
        String chatId = UUID.randomUUID().toString();
        try (InputStream is = new ClassPathResource("test.png").getInputStream()) {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image", "test.png", "image/png", is
            );
            String response = shopMateApp.doChatOfImage("描述图片", chatId, imageFile);
            assertNotNull(response);
        } catch (IOException e) {
            throw new RuntimeException("测试资源加载失败", e);
        }
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "第一次来咱们店，怎么才能找到我想要的商品？";
        String response = shopMateApp.doChatWithRag(message, chatId);
        assertNotNull(response);
    }
    @Test
    void doChatWithRagShop() {
        String chatId = UUID.randomUUID().toString();
        String message = "我喜欢鞋，给我推荐下吧";
        String response = shopMateApp.doChatWithRag(message, chatId);
        assertNotNull(response);
    }
}