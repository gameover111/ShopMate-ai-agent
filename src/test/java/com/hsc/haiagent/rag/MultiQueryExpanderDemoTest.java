package com.hsc.haiagent.rag;

import com.hsc.haiagent.demo.rag.MultiQueryExpanderDemo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {
        // 1. 传入一个口语化问题
        List<Query> expandedQueries = multiQueryExpanderDemo.expand("店小二，产品坏了怎么退？");

        // 2. 打印看看大模型帮你扩展出了哪些更精准的查询语句
//        expandedQueries.forEach(q -> System.out.println("扩展后的查询语句: " + q.text()));

        // 3. 在真实的 RAG 架构中，下一步就是：
        /*
           for (Query q : expandedQueries) {
               // 拿着这 3 个 Query 分别或者并发去向量数据库检索
               List<Document> docs = vectorStore.similaritySearch(q.text());
               // 合并所有的检索结果去重，最后喂给大模型
           }
        */
    }

}
