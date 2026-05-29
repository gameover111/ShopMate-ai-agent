package com.hsc.haiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ShopMateAppDocumentLoaderWithGitHubRepo {

    @Value("${app.github.repo:alibaba/spring-ai-alibaba}")
    private String repoName;

    @Value("${app.github.token:YOUR_PERSONAL_ACCESS_TOKEN}")
    private String githubToken;

    public List<Document> loadGitHubRepoData() {
        // 💡 这里把从配置读取到的变量传给 Reader
        GitHubRepoDocumentReader githubReader = new GitHubRepoDocumentReader(repoName, githubToken);
        return githubReader.get();
    }
}