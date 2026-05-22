package com.hsc.haiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

import java.util.Set;

@Slf4j
public class SensitiveWordAdvisor implements CallAdvisor, StreamAdvisor {

    // 违禁词库（可根据需要扩展为从配置文件或数据库加载）
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
            "暴力", "色情", "赌博", "毒品", "反动", "恐怖", "弱智", "脑残"
    );

    // 可通过构造函数注入动态词库
    private final Set<String> customForbiddenWords;

    public SensitiveWordAdvisor() {
        this.customForbiddenWords = Set.of(); // 默认空
    }

    public SensitiveWordAdvisor(Set<String> customForbiddenWords) {
        this.customForbiddenWords = customForbiddenWords != null ? customForbiddenWords : Set.of();
    }

    // 获取完整的违禁词集合
    private Set<String> getAllForbiddenWords() {
        if (customForbiddenWords.isEmpty()) {
            return FORBIDDEN_WORDS;
        }
        // 合并默认词库和自定义词库
        Set<String> merged = new java.util.HashSet<>(FORBIDDEN_WORDS);
        merged.addAll(customForbiddenWords);
        return merged;
    }

    // 前置处理：校验用户输入是否包含违禁词
    private ChatClientRequest before(ChatClientRequest request) {
        UserMessage userMessage = request.prompt().getUserMessage();
        if (userMessage == null) {
            log.warn("未获取到用户消息，跳过敏感词校验");
            return request;
        }

        String userText = userMessage.getText();
        if (userText == null || userText.trim().isEmpty()) {
            return request;
        }

        Set<String> forbiddenWords = getAllForbiddenWords();
        for (String word : forbiddenWords) {
            if (userText.contains(word)) {
                log.warn("检测到违禁词: '{}' ，用户输入: {}", word, userText);
                throw new IllegalArgumentException("输入包含违禁词: " + word);
            }
        }
        log.info("敏感词校验通过");
        return request;
    }

    // 后置处理（可选：记录响应日志）
    private void observeAfter(ChatClientResponse response) {
        if (response != null && response.chatResponse() != null) {
            log.debug("敏感词校验 Advisor 后置处理完成");
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 通常敏感词校验应该在权限校验之后，但在请求发送给 LLM 之前
        // 返回 1 表示在 PermissionAdvisor (order=0) 之后执行
        return 1;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        request = this.before(request);
        ChatClientResponse response = chain.nextCall(request);
        this.observeAfter(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        request = this.before(request);
        Flux<ChatClientResponse> responses = chain.nextStream(request);
        return responses.doOnNext(this::observeAfter);
    }
}