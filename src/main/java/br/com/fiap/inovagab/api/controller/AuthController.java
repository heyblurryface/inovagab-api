package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.LoginRequest;
import br.com.fiap.inovagab.api.dto.LoginResponse;
import br.com.fiap.inovagab.api.dto.UsuarioResponse;
import br.com.fiap.inovagab.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticacao", description = "Login (JWT) e perfil do usuario logado")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login por e-mail/senha - retorna JWT e dados do usuario (publico)")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @Operation(summary = "Perfil do usuario autenticado (com saldo de pontos atualizado)")
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Usuario usuario) {
        return authService.perfil(usuario.getId());
    }
}
