package com.CineSync.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    List<Usuario> findAllByStatus(Status status);
}
