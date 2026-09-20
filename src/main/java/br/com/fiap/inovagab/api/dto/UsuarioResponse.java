package br.com.fiap.inovagab.api.dto;

import br.com.fiap.inovagab.api.domain.Usuario;

public record UsuarioResponse(String id, String nome, String email, String role, int pontos) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole().getValue(), u.getPontos());
    }
}
