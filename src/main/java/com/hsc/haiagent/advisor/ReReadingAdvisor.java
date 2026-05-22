package com.hsc.haiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 自定义重复阅读advisor
 * 可以提升大模型的推理能力
 */
public class ReReadingAdvisor implements BaseAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
			{re2_input_query}
			Read the question again: {re2_input_query}
			""";

	private final String re2AdviseTemplate;

	private int order = 0;

	public ReReadingAdvisor() {
		this(DEFAULT_RE2_ADVISE_TEMPLATE);
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) { 
		String augmentedUserText = PromptTemplate.builder()
			.template(this.re2AdviseTemplate)
			.variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
			.build()
			.render();

		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
			.build();
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}
	// 同步调用处理（必须实现，因为 BaseAdvisor 扩展了 CallAdvisor）
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		ChatClientRequest modified = before(request, chain);
		ChatClientResponse response = chain.nextCall(modified);
		return after(response, chain);
	}
	// 异步调用处理（必须实现，因为 BaseAdvisor 扩展了 StreamAdvisor）
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {

		return  Mono.just(chatClientRequest)
				.publishOn(Schedulers.boundedElastic())
				.map(request -> {
					// This can be executed by blocking and non-blocking Threads.
					// Advisor before next section
					return before(request, chain);
				})
				.flatMapMany(request -> chain.nextStream(request))
				.map(response -> {
					// Advisor after next section
					return after(response, chain);
				});
	}
	@Override
	public int getOrder() { 
		return this.order;
	}

	public ReReadingAdvisor withOrder(int order) {
		this.order = order;
		return this;
	}

}