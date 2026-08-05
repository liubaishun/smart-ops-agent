package com.unionpay.agent.ai.controller;


import com.ai.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/document")
public class DocumentController {


    private final DocumentService service;


    public DocumentController(DocumentService service) {

        this.service = service;

    }


    @PostMapping("/upload")
    public String upload(MultipartFile file) throws Exception {


        service.upload(file);


        return "知识库上传成功";

    }

}