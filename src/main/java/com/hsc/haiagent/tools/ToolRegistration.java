package com.hsc.haiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.annotation.Resource;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {

    @Value("${app.tavily.api-key}")
    private String searchApiKey;

    // 💡 注入 Spring 的邮件发送门面组件
    @Resource
    private JavaMailSender mailSender;

    // 💡 注入你在 yml 里面配置的系统发件人邮箱
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        
        // 💡 实例化你的两个新轻量级 POJO 工具
        TimeOperationTool timeOperationTool = new TimeOperationTool();
        MailSenderTool mailSenderTool = new MailSenderTool(mailSender, fromEmail);

        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool,
                timeOperationTool, // 追加进总注册表
                mailSenderTool      // 追加进总注册表
        );
    }
}