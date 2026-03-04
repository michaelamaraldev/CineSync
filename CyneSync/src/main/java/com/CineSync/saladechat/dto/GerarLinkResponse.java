package com.CineSync.saladechat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GerarLinkResponse {
    private String userId;
    private String chatId;

    private Integer expirationHours;

    private int maxParticipants;
}