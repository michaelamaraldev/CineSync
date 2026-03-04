package com.CineSync.saladechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class SalaDeChat {
    @Id
    private String id;

    private String chatId;
    private String senderId;
    private String recipientId;

    @Indexed(unique = true)
    private String shareCode;

    private String ownerId;

    @Builder.Default
    private List<String> participants = new ArrayList<>();

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private int maxParticipants = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}