package com.hsc.haiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网络搜索工具
 * 用于从 Tavily RAG 搜索引擎获取网络搜索结果
 */
public class WebSearchTool {

    // 💡 Tavily 的专属官方 API 端点
    private static final String TAVILY_API_URL = "https://api.tavily.com/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for well-summarized and clean context from Tavily RAG Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {

        // 💡 Tavily 官方推荐使用 POST 请求，但它也完美支持标准的 JSON/表单传参
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("query", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("max_results", 5); // 限制最多取 5 条数据

        try {
            // 使用 Hutool 发送标准的 JSON POST 请求
            String response = HttpRequest.post(TAVILY_API_URL)
                    .body(JSONUtil.toJsonStr(paramMap))
                    .timeout(10000)
                    .execute()
                    .body();

            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray results = jsonObject.getJSONArray("results");

            if (results == null || results.isEmpty()) {
                return "No search answers from Tavily. Raw response: " + response;
            }

            List<Object> objects = results.subList(0, Math.min(results.size(), 5));

            return objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                Map<String, Object> cleanMap = new HashMap<>();
                cleanMap.put("title", tmpJSONObject.getStr("title"));
                cleanMap.put("content", tmpJSONObject.getStr("content"));
                return JSONUtil.toJsonStr(cleanMap);
            }).collect(Collectors.joining(","));

        } catch (Exception e) {
            return "Error searching Tavily: " + e.getMessage();
        }
    }
}