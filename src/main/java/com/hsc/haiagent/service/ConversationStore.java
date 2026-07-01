package com.hsc.haiagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话消息存储器（内存 + JDBC 持久化）。
 * HManus 的消息使用前缀 "manus_" 区分，不与 ShopMate 的 spring_ai_chat_memory 记录混在一起。
 */
@Slf4j
@Component
public class ConversationStore {

    private static final String PREFIX = "manus_";

    private final JdbcTemplate jdbcTemplate;

    private final Map<String, List<Map<String, String>>> cache = new ConcurrentHashMap<>();

    public ConversationStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 内部使用的带前缀的 key */
    private String key(String chatId) {
        return PREFIX + chatId;
    }

    private String key(String prefix, String chatId) {
        return prefix + chatId;
    }

    /** 获取编排器会话 */
    public List<Map<String, String>> getOrchestratorMessages(String chatId) {
        String k = key("orch_", chatId);
        if (cache.containsKey(k)) return cache.get(k);
        List<Map<String, String>> messages = loadFromDb(k);
        cache.put(k, messages);
        return messages;
    }

    /** 保存编排器消息 */
    public void addOrchestratorMessage(String chatId, String role, String content) {
        if (content == null) content = "";
        String k = key("orch_", chatId);
        saveToDb(k, role.equals("user") ? "USER" : "ASSISTANT", content);
        cache.computeIfAbsent(k, x -> new ArrayList<>())
                .add(Map.of("role", role, "content", content));
    }

    /**
     * 获取会话的消息列表
     */
    public List<Map<String, String>> getMessages(String chatId) {
        String k = key(chatId);
        if (cache.containsKey(k)) {
            return cache.get(k);
        }
        List<Map<String, String>> messages = loadFromDb(k);
        cache.put(k, messages);
        return messages;
    }

    public void addUserMessage(String chatId, String content) {
        if (content == null) content = "";
        String k = key(chatId);
        saveToDb(k, "USER", content);
        cache.computeIfAbsent(k, x -> new ArrayList<>())
                .add(Map.of("role", "user", "content", content));
    }

    public void addAssistantMessage(String chatId, String content) {
        if (content == null) content = "";
        String k = key(chatId);
        saveToDb(k, "ASSISTANT", content);
        cache.computeIfAbsent(k, x -> new ArrayList<>())
                .add(Map.of("role", "assistant", "content", content));
    }

    public void deleteSession(String chatId) {
        cache.remove(key(chatId));
        jdbcTemplate.update("DELETE FROM spring_ai_chat_memory WHERE conversation_id = ?", key(chatId));
    }

    // ========== 数据库 ==========

    /** 从数据库加载，conversationId 已是完整带前缀的 key */
    private List<Map<String, String>> loadFromDb(String conversationId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT type, content FROM spring_ai_chat_memory WHERE conversation_id = ? ORDER BY \"timestamp\" ASC",
                    conversationId);
            List<Map<String, String>> messages = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String type = (String) row.get("type");
                String content = (String) row.get("content");
                if (type == null || content == null) continue;
                messages.add(Map.of(
                        "role", type.equalsIgnoreCase("USER") ? "user" : "assistant",
                        "content", content
                ));
            }
            log.info("加载会话 {}: {} 条消息", conversationId, messages.size());
            return messages;
        } catch (Exception e) {
            log.warn("加载会话 {} 失败: {}", conversationId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveToDb(String conversationId, String type, String content) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO spring_ai_chat_memory (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, NOW())",
                    conversationId, content, type);
            log.debug("写入会话 {} type={} len={}", conversationId, type, content.length());
        } catch (Exception e) {
            log.error("写入失败 conversation_id={}: {}", conversationId, e.getMessage());
        }
    }
}
