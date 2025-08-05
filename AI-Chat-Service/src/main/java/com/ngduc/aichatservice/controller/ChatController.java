package com.ngduc.aichatservice.controller;

import com.ngduc.aichatservice.dto.ChatRequest;
import com.ngduc.aichatservice.dto.ChatResponse;
import com.ngduc.aichatservice.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/api/ai-chat")
@CrossOrigin(
    origins = "http://localhost:3000",
    allowedHeaders = "*",
    methods = {RequestMethod.POST, RequestMethod.OPTIONS},
    allowCredentials = "true"
)
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/ask")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return geminiService.chat(request);
    }
}