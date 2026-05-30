package com.hsc.haiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件发送工具
 */
public class MailSenderTool {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    // 💡 通过构造方法接收由 ToolRegistration 传过来的发件依赖和发件人
    public MailSenderTool(JavaMailSender mailSender, String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Tool(description = "Send a plain text email notification to a specified recipient. Ideal for notifications, alerts, order confirmations, or sending summaries.")
    public String sendTextMessage(
            @ToolParam(description = "The target recipient's email address. Must be a valid email format, e.g., user@example.com") String to,
            @ToolParam(description = "The subject or title of the email.") String subject,
            @ToolParam(description = "The main body content of the email.") String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            return "Email sent successfully to: " + to;
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
}