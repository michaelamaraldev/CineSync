package com.CineSync.chat;

import com.CineSync.saladechat.SalaDeChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class MensagemService {
    private final MensagemRepository repository;
    private final SalaDeChatService salaDeChatService;

    public Mensagem save(Mensagem mensagem) {
        var chatId = salaDeChatService
                .getChatRoomId(mensagem.getSenderId(), mensagem.getRecipientId(), true)
                .orElseThrow();
        mensagem.setChatId(chatId);
        repository.save(mensagem);
        return mensagem;
    }

    public List<Mensagem> findChatMessages(String senderId, String recipientId) {
        var chatId = salaDeChatService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }
}