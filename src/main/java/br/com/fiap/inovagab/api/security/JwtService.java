package br.com.fiap.inovagab.api.security;

import br.com.fiap.inovagab.api.config.JwtProperties;
import br.com.fiap.inovagab.api.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoHoras;

    public JwtService(JwtProperties props) {
        this.chave = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expiracaoHoras = props.expiracaoHoras();
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("role", usuario.getRole().getValue())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracaoEm(agora)))
                .signWith(chave)
                .compact();
    }

    public Instant expiracaoEm(Instant emissao) {
        return emissao.plus(expiracaoHoras, ChronoUnit.HOURS);
    }

    public String extrairUsuarioId(String token) {
        return parse(token).getSubject();
    }

    public boolean valido(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
