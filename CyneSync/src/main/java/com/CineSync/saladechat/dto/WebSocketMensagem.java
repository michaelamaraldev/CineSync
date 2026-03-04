package com.CineSync.saladechat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class WebSocketMensagem {

    public enum MessageType {
        CHAT, JOIN, LEAVE, SYSTEM
    }

    private MessageType type;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
}