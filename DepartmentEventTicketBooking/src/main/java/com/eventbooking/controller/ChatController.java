package com.eventbooking.controller;

import com.eventbooking.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> receiveMessage(@RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "");
        String response = chatService.getBotResponse(message);
        return Collections.singletonMap("response", response);
    }
}
