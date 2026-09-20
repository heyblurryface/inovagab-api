package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.LoginRequest;
import br.com.fiap.inovagab.api.dto.LoginResponse;
import br.com.fiap.inovagab.api.dto.UsuarioResponse;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.repository.UsuarioRepository;
import br.com.fiap.inovagab.api.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha invalidos"));

        if (!usuario.isAtivo() || !passwordEncoder.matches(req.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("E-mail ou senha invalidos");
        }

        Instant agora = Instant.now();
        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.expiracaoEm(agora), UsuarioResponse.de(usuario));
    }

    /** Recarrega o perfil do banco (pontos atualizados). */
    public UsuarioResponse perfil(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
        return UsuarioResponse.de(usuario);
    }
}
