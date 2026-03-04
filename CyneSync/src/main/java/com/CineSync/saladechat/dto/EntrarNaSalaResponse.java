package com.CineSync.saladechat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class EntrarNaSalaResponse {
    private String chatId;
    private String shareCode;
    private String userId;
    private List<String> participants;
    private String websocketTopic;
    private String message;
}