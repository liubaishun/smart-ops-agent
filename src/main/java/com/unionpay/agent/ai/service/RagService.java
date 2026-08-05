package com.unionpay.agent.ai.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;


@Service
public class RagService {


    private final VectorStore vectorStore;


    private final ChatClient chatClient;


    public RagService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }


    public String ask(String question) {


        List<Document> docs = vectorStore.similaritySearch(question);


        String context = docs.stream().map(Document::getText)
                .collect(Collectors.joining("\n"));


        String prompt = String.format(
                "你是一名制造企业设备维修专家。\n\n" +
                        "根据下面设备说明书回答问题：\n\n" +
                        "%s\n\n" +
                        "用户问题：\n\n" +
                        "%s\n\n" +
                        "要求：\n" +
                        "1. 只根据资料回答\n" +
                        "2. 不允许编造\n" +
                        "3. 给出具体维修步骤",
                context,
                question
        );


        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

    }

}