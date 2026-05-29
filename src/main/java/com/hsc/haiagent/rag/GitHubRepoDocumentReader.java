package com.hsc.haiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitHubRepoDocumentReader implements DocumentReader {

    private final String repoName;
    private final String githubToken;
    private final RestTemplate restTemplate = new RestTemplate();

    public GitHubRepoDocumentReader(String repoName, String githubToken) {
        this.repoName = repoName;
        this.githubToken = githubToken;
    }

    @Override
    public List<Document> get() {
        List<Document> documents = new ArrayList<>();
        try {
            // 1. 构建标准的 GitHub API 请求头，带上你的令牌凭证
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            // 官方推荐的最佳实践 Accept 头
            headers.set("Accept", "application/vnd.github+json"); 
            // 加上 User-Agent，防止 GitHub 接口因为没有代理头直接秒回 403
            headers.set("User-Agent", "Spring-AI-Agent"); 
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 2. 拼接 GitHub 官方标准的 Issues 接口 URL（这里默认拿最新的 10 条开放状态数据）
            String url = "https://api.github.com/repos/" + repoName + "/issues?state=open&per_page=10";

            // 3. 直接通过 RestTemplate 发送请求，用万能的 List<Map> 接收，完美跳过对象映射层
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> issues = (List<Map<String, Object>>) response.getBody();

            if (issues != null) {
                for (Map<String, Object> issue : issues) {
                    // ⚠️ 过滤掉 Pull Request（GitHub 底层把 PR 也当作一种特定的 Issue 返回）
                    if (issue.containsKey("pull_request")) {
                        continue;
                    }

                    String title = (String) issue.get("title");
                    String body = (String) issue.get("body");
                    String htmlUrl = (String) issue.get("html_url");
                    Map<String, Object> user = (Map<String, Object>) issue.get("user");

                    // 组装最终喂给 RAG 的文本内容
                    String content = String.format("Title: %s\n\nBody:\n%s", title, body != null ? body : "No content");

                    // 4. 装填元数据，继续保留你的 "品列" 标签逻辑
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", "github-issue");
                    metadata.put("repo", repoName);
                    metadata.put("html_url", htmlUrl);
                    metadata.put("author", user != null ? user.get("login") : "unknown");
                    metadata.put("status", "品列"); 

                    documents.add(new Document(content, metadata));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("通过 API 请求远程 GitHub 仓库数据失败: " + repoName, e);
        }
        return documents;
    }
}