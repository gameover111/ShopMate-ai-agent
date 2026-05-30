package com.hsc.haiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@SpringBootTest()
@TestPropertySource(locations = "classpath:application-local.yml")
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
        // 💡 明确提及关键词，引导向量检索，并要求它结合商品库推荐
        String message = "我是售前客服，有顾客让我推荐鞋子。请结合咱们店铺在售的商品清单知识库，给我推荐两款适合日常穿的舒适运动鞋。";
        String response = shopMateApp.doChatWithRag(message, chatId);
        System.out.println("AI 最终回复：\n" + response);
        assertNotNull(response);
    }

    @Test
    void doChatWithTools() {

// 💡 场景 1：测试 RAG 向量检索（鞋服搭配、小众推荐）
//        testMessage("我夏天喜欢穿宽松的工装裤，推荐几双适合搭配、比较小众的复古运动鞋？");

        // 💡 场景 2：测试 WebSearchTool 联网搜索（通过搜索引擎查外部知名电商/科技资讯）
//        testMessage("帮我联网查一下，最近程序员鱼皮的编程导航（codefather.cn）上有什么关于球鞋抢购脚本的讨论吗？");

        // 💡 场景 3：测试大模型图片生成/下载工具（电商商品图、壁纸生成）
//        testMessage("直接下载一张高画质的、适合做手机壁纸的‘极简风耐克AJ球鞋特写图片’为文件");

        // 💡 场景 4：测试 CodeInterpreter 跑 Python 脚本（电商销售数据分析）
//        testMessage("执行 Python3 脚本来分析我们店铺上个月‘运动鞋类目’的销售趋势并生成分析报告");

        // 💡 场景 5：测试本地数据库/文件保存（客户的核心尺码与购买偏好档案）
//        testMessage("将我的‘运动装备尺码与偏好档案（身高180, 鞋码43, 偏好耐克）’保存为本地文件");

        // 💡 场景 6：测试复杂工具/PDF生成（电商营销策划案、商品清单导出）
//        testMessage("生成一份‘618店铺运动鞋大促运营计划’PDF，包含主推款式、折扣力度和赠品清单");
        // ==================== ✨ 新增实用智能体工具测试场景 ====================

        // 💡 场景 7：测试 TimeOperationTool 时间感知工具
        // 大模型无法凭空知道今天几号，通过此测试可以验证大模型是否会主动调用时间工具来确定今天的日期
        System.out.println("\n=== 🧪 运行场景 7：时间日期感知测试 ===");
        testMessage("今天是几号？星期几？如果要算到今年国庆节还有多少天？");

        // 💡 场景 8：测试 MailSenderTool 物理动作落地工具（发信通知）
        // ⚠️ 测试时，请把收件人邮箱改成你自己真实的测试邮箱
        System.out.println("\n=== 🧪 运行场景 8：邮件物理发送测试 ===");
        testMessage("给我的客户 1476889669@qq.com 发一封邮件，主题是‘ShopMate 尺码档案确认’，正文写‘亲，您登记的鞋码43偏好耐克已录入成功，后续有折扣会第一时间通知您’。");

        // 💡 场景 9：测试【时间感知 + 联网搜索 + 邮件通知】高级多工具自主联动（链式调用）
        // 这是对 ToolCallingManager 综合调度能力最严苛的考验
        System.out.println("\n=== 🧪 运行场景 9：高级多工具自主联动大测试 ===");
        testMessage("现在是几点？帮我用网络搜索查一下今天‘耐克官网’有什么新的限量款AJ发售消息吗？如果有，就把发售情报整理好，发封邮件告诉我的朋友 1476889669@qq.com。");//1793297870@qq.com
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = shopMateApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
}