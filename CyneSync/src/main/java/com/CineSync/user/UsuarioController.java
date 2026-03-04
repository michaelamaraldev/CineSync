package com.CineSync.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @MessageMapping("/user.adicionarUsuario")
    @SendTo("/user/public")
    public Usuario adicionarUsuario(
            @Payload Usuario usuario
    ) {
        usuarioService.salvarUsuario(usuario);
        return usuario;
    }

    @MessageMapping("/user.desconectarUsuario")
    @SendTo("/user/public")
    public Usuario desconectarUsuario(
            @Payload Usuario usuario
    ) {
        usuarioService.desconectarUsuario(usuario);
        return usuario;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> acharUsuariosConectados() {
        return ResponseEntity.ok(usuarioService.acharUsuarios());
    }
}