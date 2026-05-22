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

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return -1;
	}

	// 前置处理：打印请求的 context
	private ChatClientRequest before(ChatClientRequest request) {
		log.info("AI Request: {}", request.prompt().getUserMessage().getText());
		return request;
	}

	// 后置处理：打印AI响应的文本内容
	private void observeAfter(ChatClientResponse response) {
		if (response != null && response.chatResponse() != null) {
			log.info("AI Response: {}", response.chatResponse().getResult().getOutput().getText());
		}
	}

	// 非流式调用处理
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		request = this.before(request);
		ChatClientResponse response = chain.nextCall(request);
		this.observeAfter(response);
		return response;
	}

	// 流式调用处理
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
		request = this.before(request);
		Flux<ChatClientResponse> responses = chain.nextStream(request);
		// 使用 MessageAggregator 聚合流式响应的片段，并在聚合完成后调用 observeAfter
		return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, this::observeAfter);
	}
}