package com.CineSync.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MensagemController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MensagemService mensagemService;

    @MessageMapping("/chat")
    public void processMessage(@Payload Mensagem mensagem) {
        Mensagem mensagemSalva = mensagemService.save(mensagem);
        messagingTemplate.convertAndSendToUser(
                mensagem.getRecipientId(), "/fila/mensagens",
                new NotificacaoChat(
                        mensagemSalva.getId(),
                        mensagemSalva.getSenderId(),
                        mensagemSalva.getRecipientId(),
                        mensagemSalva.getContent()
                )
        );
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Mensagem>> findChatMessages(@PathVariable String senderId,
                                                              @PathVariable String recipientId) {
        return ResponseEntity
                .ok(mensagemService.findChatMessages(senderId, recipientId));
    }
}