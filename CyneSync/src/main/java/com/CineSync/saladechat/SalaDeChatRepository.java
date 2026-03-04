package com.CineSync.saladechat;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SalaDeChatRepository extends MongoRepository<SalaDeChat, String> {
    Optional<SalaDeChat> findBySenderIdAndRecipientId(String senderId, String recipientId);
}