package com.CineSync.saladechat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class SalaDeChatService {

    private final SalaDeChatRepository salaDeChatRepository;

    public Optional<String> getChatRoomId(
            String senderId,
            String recipientId,
            boolean createNewRoomIfNotExists
    ) {
        return salaDeChatRepository
                .findBySenderIdAndRecipientId(senderId, recipientId)
                .map(SalaDeChat::getChatId)
                .or(() -> {
                    if(createNewRoomIfNotExists) {
                        var chatId = createChatId(senderId, recipientId);
                        return Optional.of(chatId);
                    }

                    return Optional.empty();
                });
    }

    private String createChatId(String senderId, String recipientId) {
        var chatId = String.format("%s_%s", senderId, recipientId);

        SalaDeChat senderRecipient = SalaDeChat
                .builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();

        SalaDeChat recipientSender = SalaDeChat
                .builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();

        salaDeChatRepository.save(senderRecipient);
        salaDeChatRepository.save(recipientSender);

        return chatId;
    }
}