package com.unionpay.agent.ai.controller;


import com.ai.service.RagService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/chat")
public class ChatController {


    private final RagService service;


    public ChatController(RagService service) {

        this.service = service;

    }


    @GetMapping("/ask")
    public String ask(@RequestParam String question) {


        return service.ask(question);

    }


}