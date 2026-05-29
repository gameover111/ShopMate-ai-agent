package com.hsc.haiagent.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Component
public class TranslationGateway {

    @Value("${app.baidu.appid:}")
    private String appId;

    @Value("${app.baidu.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 💡 调用百度翻译 API：将中文提问转换为英文检索词（每月 100 万字免费）
     */
    public String translateToEnglish(String text) {
        // 健壮性防崩溃：如果没配密钥，直接返回原文，确保系统不报异常
        if (appId == null || appId.isEmpty() || text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            String url = "https://fanyi-api.baidu.com/api/trans/vip/translate";

            // 1. 准备百度官方要求的签名参数
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = computeMdfSign(appId, text, salt, secretKey);

            // 2. 组装 Form 表单提交参数
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("q", text);
            params.add("from", "zh"); // 源语言：中文
            params.add("to", "en");   // 目标语言：英文
            params.add("appid", appId);
            params.add("salt", salt);
            params.add("sign", sign);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 3. 发送 POST 请求并解析返回值
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("trans_result")) {
                List<Map<String, Object>> transResult = (List<Map<String, Object>>) body.get("trans_result");
                if (transResult != null && !transResult.isEmpty()) {
                    // 完美拿到翻译后的英文结果
                    return String.valueOf(transResult.get(0).get("dst"));
                }
            }
        } catch (Exception e) {
            // 降级策略：接口抖动时直接返回原文，确保 RAG 链路不雪崩
            return text;
        }
        return text;
    }

    /**
     * 百度翻译 MD5 签名算法：sign = MD5(appid + q + salt + secretKey)
     */
    private String computeMdfSign(String appId, String q, String salt, String secretKey) throws Exception {
        String src = appId + q + salt + secretKey;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(src.getBytes(StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}