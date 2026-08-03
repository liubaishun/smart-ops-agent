package com.unionpay.agent.config;

import com.unionpay.agent.service.DiagnoseAgent;
import com.unionpay.agent.tools.DiagnoseTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfiguration {

    @Value("${smart-ops.agent.llm.base-url}")
    private String baseUrl;

    @Value("${smart-ops.agent.llm.api-key}")
    private String apiKey;

    @Value("${smart-ops.agent.llm.model-name}")
    private String modelName;

    @Value("${smart-ops.agent.llm.temperature}")
    private Double temperature;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)   // 控制台打印 Agent 请求 Trace
                .logResponses(true)  // 控制台打印 Agent 思考 Trace
                .build();
    }

    @Bean
    public DiagnoseAgent diagnoseAgent(OpenAiChatModel chatModel, DiagnoseTools diagnoseTools) {
        return AiServices.builder(DiagnoseAgent.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // 保留近期 10 条交互上下文
                .tools(diagnoseTools)                                    // 挂载自定义 Function Calling
                .build();
    }
}