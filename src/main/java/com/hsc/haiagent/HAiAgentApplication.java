package com.hsc.haiagent;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication()
public class HAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HAiAgentApplication.class, args
        );
    }
}