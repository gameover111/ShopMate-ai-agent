package com.hsc.haiagent.controller;

import com.hsc.haiagent.entity.ChatSession;
import com.hsc.haiagent.service.ChatSessionService;
import com.hsc.haiagent.service.ConversationStore;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话管理控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatMemory chatMemory;

    @Resource
    private ConversationStore conversationStore;

    /**
     * 获取会话的消息历史（只返回用户和助手的文本消息）
     */
    @GetMapping("/sessions/{id}/messages")
    public List<Map<String, Object>> getSessionMessages(@PathVariable String id) {
        List<Message> allMessages = chatMemory.get(id);
        return allMessages.stream()
                .filter(msg -> {
                    String type = msg.getMessageType().name();
                    // 只保留用户消息和助手的纯文本回复，过滤工具调用/工具结果/系统提示
                    return type.equals("USER") || type.equals("ASSISTANT");
                })
                .map(msg -> Map.<String, Object>of(
                        "role", msg.getMessageType().name().toLowerCase(),
                        "content", msg.getText() != null ? msg.getText() : ""
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前用户的会话列表（按类型过滤）
     * @param type 会话类型：shop_mate / manus / orchestrator
     */
    @GetMapping("/sessions")
    public List<ChatSession> listSessions(Authentication auth,
                                          @RequestParam(defaultValue = "shop_mate") String type) {
        if (auth == null) return List.of();
        Long userId = (Long) auth.getPrincipal();
        return chatSessionService.listSessions(userId, type);
    }

    /**
     * 创建新会话
     * @param type 会话类型：shop_mate / manus / orchestrator
     */
    @PostMapping("/sessions")
    public ChatSession createSession(Authentication auth,
                                     @RequestParam(defaultValue = "shop_mate") String type) {
        if (auth == null) throw new RuntimeException("请先登录");
        Long userId = (Long) auth.getPrincipal();
        return chatSessionService.createSession(userId, type);
    }

    /**
     * 重命名会话
     */
    @PutMapping("/sessions/{id}")
    public ChatSession renameSession(@PathVariable String id,
                                      Authentication auth,
                                      @RequestBody Map<String, String> body) {
        if (auth == null) throw new RuntimeException("请先登录");
        Long userId = (Long) auth.getPrincipal();
        return chatSessionService.renameSession(id, userId, body.get("title"));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{id}")
    public Map<String, String> deleteSession(@PathVariable String id,
                                              Authentication auth) {
        if (auth == null) throw new RuntimeException("请先登录");
        Long userId = (Long) auth.getPrincipal();
        chatSessionService.deleteSession(id, userId);
        // 清理 ConversationStore 中的 HManus/编排器 会话消息
        conversationStore.deleteSession(id);
        return Map.of("message", "会话已删除");
    }
}
