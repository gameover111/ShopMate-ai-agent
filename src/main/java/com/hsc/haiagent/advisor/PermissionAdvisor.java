package com.hsc.haiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.Set;

@Slf4j
public class PermissionAdvisor implements CallAdvisor, StreamAdvisor {
    // 设置一个 Key 常量，用来从 context map 中存取权限
    private static final String USER_PERMISSIONS = "user_permissions";


    // 前置处理：权限校验
    private ChatClientRequest before(ChatClientRequest request) {
        Object permissionsObj = request.context().get(USER_PERMISSIONS);

        if (permissionsObj == null) {
            throw new SecurityException("无权限访问 AI 服务：用户未认证");
        }
        @SuppressWarnings("unchecked")
        Set<String> permissions = (Set<String>) permissionsObj;
        if (permissions.isEmpty() || !permissions.contains("AI_CHAT")) {
            throw new SecurityException("无权限访问 AI 服务：当前用户缺少 AI_CHAT 权限");
        }
        log.info("权限校验通过，用户权限: {}", permissions);
        return request;
    }

    // 后置处理（本例仅打日志，可以记录被调用的结果）
    private void observeAfter(ChatClientResponse response) {
        if (response != null && response.chatResponse() != null) {
            log.info("权限校验 Advisor 后置处理，响应已返回");
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0; // 可以设置优先级，权限校验一般最优先
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
        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::observeAfter);
    }
}