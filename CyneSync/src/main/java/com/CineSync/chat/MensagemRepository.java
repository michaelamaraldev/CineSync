package com.CineSync.chat;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MensagemRepository extends MongoRepository<Mensagem, String> {
    List<Mensagem> findByChatId(String chatId);
}