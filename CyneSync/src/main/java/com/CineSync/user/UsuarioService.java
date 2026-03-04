package com.CineSync.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor

public class UsuarioService {

    private final UsuarioRepository repository;

    public void salvarUsuario(Usuario usuario) {
        // salvar o usuário no banco de dados
        usuario.setStatus(Status.ONLINE);
        repository.save(usuario);
    }

    public void desconectarUsuario(Usuario usuario) {
        // desconectar o usuário do sistema
        var usuarioArmazenado = repository.findById(usuario.getNickName()).orElse(null);
        if (usuarioArmazenado != null) {
            usuarioArmazenado.setStatus(Status.OFFLINE);
            repository.save(usuarioArmazenado);
        }
    }

    public List<Usuario> acharUsuarios() {
        // retornar a lista de usuários conectados
        return repository.findAllByStatus(Status.ONLINE);
    }
}
