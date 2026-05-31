package com.hsc.himagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    @Value("${app.pexels.api-key}")
    private String apiKey;

    
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
//            String result = String.join(",", searchMediumImages(query));
//            return result;
            List<String> imageUrls = searchMediumImages(query); // 拿到你的 URL 列表
            //  现在的做法：直接拼装成标准的 Markdown 图片语法！
            // 比如拼成： "![运动鞋](http://xxx.com/1.jpg)\n![运动鞋](http://xxx.com/2.jpg)"
            String markdownResult = imageUrls.stream()
                    .map(url -> "![运动鞋](" + url + ")")
                    .collect(Collectors.joining("\n"));

            return markdownResult; // 依然返回 String，但大模型一看就知道这是图！

        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    
    public List<String> searchMediumImages(String query) {
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);

        
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
