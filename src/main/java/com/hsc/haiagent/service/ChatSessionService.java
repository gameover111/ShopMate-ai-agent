package com.hsc.haiagent.service;

import com.hsc.haiagent.entity.ChatSession;
import com.hsc.haiagent.repository.ChatSessionRepository;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionService {

    @Resource
    private ChatSessionRepository sessionRepository;

    @Resource
    private ChatMemory chatMemory;

    /**
     * 获取用户的会话（按类型过滤，按更新时间倒序）
     */
    public List<ChatSession> listSessions(Long userId, String type) {
        return sessionRepository.findByUserIdAndTypeOrderByUpdatedAtDesc(userId, type);
    }

    /**
     * 创建新会话
     */
    public ChatSession createSession(Long userId, String type) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setTitle("新会话");
        session.setType(type);
        return sessionRepository.save(session);
    }

    /**
     * 重命名会话
     */
    public ChatSession renameSession(String sessionId, Long userId, String title) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此会话");
        }
        session.setTitle(title);
        return sessionRepository.save(session);
    }

    /**
     * 删除会话（同时清理聊天记忆）
     */
    @Transactional
    public void deleteSession(String sessionId, Long userId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此会话");
        }
        chatMemory.clear(sessionId);
        sessionRepository.delete(session);
    }
}
