package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Ideia de inovacao / problema registrado por um operador. */
@Document("ideias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ideia {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String pilar;
    private String orientacaoId;      // vinculo com a estrategia vigente
    private String orientacaoTitulo;
    private String autorId;
    private String autorNome;
    private StatusIdeia status;
    private int prioridade;           // 0 = nao priorizada, 1-5
    private AvaliacaoIa avaliacaoIa;
    private Instant criadoEm;
    private Instant atualizadoEm;
}
