package com.CineSync.saladechat;

import com.CineSync.saladechat.dto.*;
import com.CineSync.saladechat.exception.SalaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class SalaDeChatService {

    private final SalaDeChatRepository salaDeChatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Caracteres para gerar código curto (sem ambíguos: 0/O, 1/l/I)
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    // ===== MÉTODOS EXISTENTES (mantidos) =====

    public Optional<String> getChatRoomId(
            String senderId,
            String recipientId,
            boolean createNewRoomIfNotExists
    ) {
        return salaDeChatRepository
                .findBySenderIdAndRecipientId(senderId, recipientId)
                .map(SalaDeChat::getChatId)
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        var chatId = createChatId(senderId, recipientId);
                        return Optional.of(chatId);
                    }
                    return Optional.empty();
                });
    }

    private String createChatId(String senderId, String recipientId) {
        var chatId = String.format("%s_%s", senderId, recipientId);

        SalaDeChat senderRecipient = SalaDeChat.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .ownerId(senderId)
                .participants(new ArrayList<>())
                .build();
        senderRecipient.getParticipants().add(senderId);

        SalaDeChat recipientSender = SalaDeChat.builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .ownerId(senderId)
                .participants(new ArrayList<>())
                .build();
        recipientSender.getParticipants().add(senderId);

        salaDeChatRepository.save(senderRecipient);
        salaDeChatRepository.save(recipientSender);

        return chatId;
    }

    public LinkCompartilhavelResponse generateShareLink(GerarLinkRequest request) {
        SalaDeChat sala = salaDeChatRepository.findByChatId(request.getChatId())
                .orElseThrow(() -> new SalaException("Sala não encontrada: " + request.getChatId()));

        // Verifica se o solicitante é o dono
        if (!sala.getOwnerId().equals(request.getUserId())) {
            throw new SalaException("Apenas o dono da sala pode gerar links compartilháveis");
        }

        String shareCode = sala.getShareCode();
        if (shareCode == null || shareCode.isEmpty()) {
            shareCode = generateUniqueCode();
        }

        sala.setShareCode(shareCode);
        sala.setActive(true);
        sala.setMaxParticipants(request.getMaxParticipants());

        if (request.getExpirationHours() != null && request.getExpirationHours() > 0) {
            sala.setExpiresAt(LocalDateTime.now().plusHours(request.getExpirationHours()));
        }

        salaDeChatRepository.save(sala);

        String fullLink = String.format("%s/chat/join/%s", baseUrl, shareCode);

        log.info("Link gerado para sala {}: {}", sala.getChatId(), fullLink);

        return LinkCompartilhavelResponse.builder()
                .shareCode(shareCode)
                .fullLink(fullLink)
                .chatId(sala.getChatId())
                .ownerId(sala.getOwnerId())
                .expiresAt(sala.getExpiresAt())
                .active(sala.isActive())
                .build();
    }

    public LinkCompartilhavelResponse createRoomWithShareLink(String ownerId) {
        String shareCode = generateUniqueCode();
        String chatId = "room_" + shareCode;

        SalaDeChat sala = SalaDeChat.builder()
                .chatId(chatId)
                .senderId(ownerId)
                .ownerId(ownerId)
                .shareCode(shareCode)
                .participants(new ArrayList<>())
                .active(true)
                .maxParticipants(0)
                .createdAt(LocalDateTime.now())
                .build();

        sala.getParticipants().add(ownerId);
        salaDeChatRepository.save(sala);

        String fullLink = String.format("%s/chat/join/%s", baseUrl, shareCode);

        log.info("Sala criada com link: {} por usuário: {}", fullLink, ownerId);

        return LinkCompartilhavelResponse.builder()
                .shareCode(shareCode)
                .fullLink(fullLink)
                .chatId(chatId)
                .ownerId(ownerId)
                .active(true)
                .build();
    }

    public EntrarNaSalaResponse joinRoomByShareCode(String shareCode, String userId) {
        SalaDeChat sala = salaDeChatRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new SalaException("Link inválido ou sala não encontrada"));


        validateRoom(sala, userId);

        if (!sala.getParticipants().contains(userId)) {
            sala.getParticipants().add(userId);
            salaDeChatRepository.save(sala);

            notifyParticipants(sala.getChatId(), userId, "entrou na sala");
        }

        String websocketTopic = "/topic/room/" + sala.getChatId();

        log.info("Usuário {} entrou na sala {} via link {}", userId, sala.getChatId(), shareCode);

        return EntrarNaSalaResponse.builder()
                .chatId(sala.getChatId())
                .shareCode(shareCode)
                .userId(userId)
                .participants(sala.getParticipants())
                .websocketTopic(websocketTopic)
                .message("Você entrou na sala com sucesso!")
                .build();
    }

    public void revokeShareLink(String chatId, String userId) {
        SalaDeChat sala = salaDeChatRepository.findByChatId(chatId)
                .orElseThrow(() -> new SalaException("Sala não encontrada"));

        if (!sala.getOwnerId().equals(userId)) {
            throw new SalaException("Apenas o dono da sala pode revogar o link");
        }

        sala.setShareCode(null);
        sala.setActive(false);
        salaDeChatRepository.save(sala);

        notifyParticipants(chatId, "Sistema", "O link compartilhável foi desativado");

        log.info("Link revogado para sala {} pelo usuário {}", chatId, userId);
    }

    public LinkCompartilhavelResponse regenerateShareLink(String chatId, String userId) {
        SalaDeChat sala = salaDeChatRepository.findByChatId(chatId)
                .orElseThrow(() -> new SalaException("Sala não encontrada"));

        if (!sala.getOwnerId().equals(userId)) {
            throw new SalaException("Apenas o dono da sala pode regenerar o link");
        }

        String newCode = generateUniqueCode();
        sala.setShareCode(newCode);
        sala.setActive(true);
        salaDeChatRepository.save(sala);

        String fullLink = String.format("%s/chat/join/%s", baseUrl, newCode);

        log.info("Link regenerado para sala {}: {}", chatId, fullLink);

        return LinkCompartilhavelResponse.builder()
                .shareCode(newCode)
                .fullLink(fullLink)
                .chatId(chatId)
                .ownerId(userId)
                .active(true)
                .build();
    }

    private void validateRoom(SalaDeChat sala, String userId) {
        if (!sala.isActive()) {
            throw new SalaException("Esta sala não está mais aceitando novos participantes");
        }

        if (sala.getExpiresAt() != null && LocalDateTime.now().isAfter(sala.getExpiresAt())) {
            sala.setActive(false);
            salaDeChatRepository.save(sala);
            throw new SalaException("Este link expirou");
        }

        if (sala.getMaxParticipants() > 0
                && sala.getParticipants().size() >= sala.getMaxParticipants()
                && !sala.getParticipants().contains(userId)) {
            throw new SalaException(
                    String.format("Sala lotada (%d/%d participantes)",
                            sala.getParticipants().size(),
                            sala.getMaxParticipants())
            );
        }
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = generateRandomCode();
            attempts++;
            if (attempts > 100) {
                throw new SalaException("Não foi possível gerar um código único");
            }
        } while (salaDeChatRepository.existsByShareCode(code));

        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private void notifyParticipants(String chatId, String userId, String message) {
        WebSocketMensagem wsMessage = WebSocketMensagem.builder()
                .type(WebSocketMensagem.MessageType.SYSTEM)
                .chatId(chatId)
                .senderId(userId)
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + chatId, wsMessage);
    }
}