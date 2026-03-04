package com.CineSync.saladechat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class GerarLinkRequest {
    private String chatId;
    private String userId;
    private Integer expirationHours;
    private Integer maxParticipants;
}
