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

public class LinkCompartilhavelResponse {
    private String shareCode;
    private String fullLink;
    private String chatId;
    private String ownerId;
    private LocalDateTime expiresAt;
    private boolean active;
}
