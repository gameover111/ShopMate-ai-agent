package com.hsc.haiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // 1. 创建底层存储仓库 (In-Memory)
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // 2. 创建“滑动窗口”管理器，并设置最多保留 20 条消息
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20) // 这个数字可根据你的上下文长度调整
                .build();

        // 3. 装配 ChatClient，注意这里使用了 .builder() 模式来构建 Advisor
        return builder
                .defaultSystem("你是一个友好的智能助手，请用中文回答问题。")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 这是新版用法
                )
                .build();
    }
}
