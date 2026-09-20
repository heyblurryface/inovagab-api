package br.com.fiap.inovagab.api.security;

import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Le o header Authorization: Bearer <token>, valida o JWT e carrega o usuario
 * (do banco) no SecurityContext. O principal da requisicao passa a ser a entidade Usuario,
 * o que permite usar @AuthenticationPrincipal Usuario nos controllers.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(PREFIXO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIXO.length()).trim();
            if (jwtService.valido(token)) {
                String usuarioId = jwtService.extrairUsuarioId(token);
                usuarioRepository.findById(usuarioId)
                        .filter(Usuario::isAtivo)
                        .ifPresent(usuario -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    usuario, null,
                                    List.of(new SimpleGrantedAuthority(usuario.getRole().authority())));
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }

        chain.doFilter(request, response);
    }
}
