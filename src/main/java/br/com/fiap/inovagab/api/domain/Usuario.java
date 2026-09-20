package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    private String id;
    private String nome;
    @Indexed(unique = true)
    private String email;
    @JsonIgnore
    private String senhaHash;
    private Role role;
    private int pontos;
    private boolean ativo;
    private Instant criadoEm;
}
