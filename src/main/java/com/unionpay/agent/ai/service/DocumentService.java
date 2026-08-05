package com.unionpay.agent.ai.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import org.apache.pdfbox.Loader;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.List;


@Service
public class DocumentService {


    private final VectorStore vectorStore;


    public DocumentService(VectorStore vectorStore) {

        this.vectorStore = vectorStore;

    }


    public void upload(MultipartFile file) throws Exception {

        PDDocument pdf = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();

        String text = stripper.getText(pdf);

        List<Document> docs = split(text);

        vectorStore.add(docs);

        pdf.close();

    }


    private List<Document> split(String text) {


        List<Document> result = new ArrayList<>();


        int size = 800;


        for (int i = 0; i < text.length(); i += size) {


            int end = Math.min(i + size, text.length());


            result.add(

                    new Document(text.substring(i, end))

            );

        }


        return result;

    }

}