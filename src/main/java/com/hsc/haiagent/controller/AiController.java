package com.hsc.haiagent.controller;

import com.hsc.haiagent.agent.HManus;
import com.hsc.haiagent.app.ShopMateApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        HManus hManus = new HManus(allTools, dashscopeChatModel);
        return hManus.runStream(message);
    }





    @Resource
    private ShopMateApp shopMateApp;
    /**
     * 同步调用ShopMateApp的doChat方法
     * @param message 用户消息
     * @param chatId 聊天ID
     * @return ShopMateApp的响应
     */
    @GetMapping("/shop_mate_app/chat/sync")
    public String dochatWithShopMateAppSync(String message, String chatId) {
        return shopMateApp.doChat(message, chatId);
    }
    /**
     * 流式输出调用ShopMateApp的doChatByStream方法
     * @param message 用户消息
     * @param chatId 聊天ID
     * @return 流式输出的客服回复
     */
    @GetMapping(value = "/shop_mate_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> dochatWithShopMateAppSSE(String message, String chatId) {
        return shopMateApp.doChatByStream(message, chatId);
    }
    /**
     * 流式输出调用ShopMateApp的doChatByStream方法
     * @param message 用户消息
     * @param chatId 聊天ID
     * @return 流式输出的客服回复
     */
    @GetMapping(value = "/shop_mate_app/chat/sse")
    public Flux<ServerSentEvent<String>> dochatWithShopMateAppServerSentEvent(String message, String chatId) {
        return shopMateApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
    /**
     * 流式输出调用ShopMateApp的doChatByStream方法
     * @param message 用户消息
     * @param chatId 聊天ID
     * @return 流式输出的客服回复
     */
    @GetMapping("/shop_mate_app/chat/sse/emitter")
    public SseEmitter doChatWithShopMateAppSseEmitter(String message, String chatId) {

        SseEmitter emitter = new SseEmitter(180000L);

        shopMateApp.doChatByStream(message, chatId)
                .subscribe(

                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },

                        emitter::completeWithError,

                        emitter::complete
                );

        return emitter;
    }

}
