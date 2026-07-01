package com.hsc.haiagent.repository;

import com.hsc.haiagent.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findByUserIdAndTypeOrderByUpdatedAtDesc(Long userId, String type);

    void deleteByIdAndUserId(String id, Long userId);
}
