// src/main/java/com/CineSync/saladechat/SalaDeChatController.java
package com.CineSync.saladechat;

import com.CineSync.saladechat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaDeChatController {

    private final SalaDeChatService salaDeChatService;

    @PostMapping("/rooms")
    public ResponseEntity<LinkCompartilhavelResponse> createRoom(@RequestParam String ownerId) {
        LinkCompartilhavelResponse response = salaDeChatService.createRoomWithShareLink(ownerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/share-link")
    public ResponseEntity<LinkCompartilhavelResponse> generateShareLink(
            @RequestBody GerarLinkRequest request
    ) {
        LinkCompartilhavelResponse response = salaDeChatService.generateShareLink(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{shareCode}")
    public ResponseEntity<EntrarNaSalaResponse> joinRoom(
            @PathVariable String shareCode,
            @RequestParam String userId
    ) {
        EntrarNaSalaResponse response = salaDeChatService.joinRoomByShareCode(shareCode, userId);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/rooms/{chatId}/share-link")
    public ResponseEntity<Void> revokeLink(
            @PathVariable String chatId,
            @RequestParam String userId
    ) {
        salaDeChatService.revokeShareLink(chatId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rooms/{chatId}/share-link")
    public ResponseEntity<LinkCompartilhavelResponse> regenerateLink(
            @PathVariable String chatId,
            @RequestParam String userId
    ) {
        LinkCompartilhavelResponse response = salaDeChatService.regenerateShareLink(chatId, userId);
        return ResponseEntity.ok(response);
    }

    @MessageMapping("/room/{chatId}/send")
    @SendTo("/topic/room/{chatId}")
    public WebSocketMensagem sendMessage(
            @DestinationVariable String chatId,
            @Payload WebSocketMensagem message
    ) {
        message.setChatId(chatId);
        message.setTimestamp(java.time.LocalDateTime.now());
        return message;
    }

    @MessageMapping("/room/{chatId}/join")
    @SendTo("/topic/room/{chatId}")
    public WebSocketMensagem userJoined(
            @DestinationVariable String chatId,
            @Payload WebSocketMensagem message
    ) {
        message.setType(WebSocketMensagem.MessageType.JOIN);
        message.setChatId(chatId);
        message.setContent(message.getSenderName() + " entrou na sala");
        message.setTimestamp(java.time.LocalDateTime.now());
        return message;
    }
}