package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Resultado da pontuacao/priorizacao automatica de uma ideia feita por IA. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoIa {
    private int pontuacao;               // 0-100
    private int alinhamentoEstrategico;  // 0-100
    private int viabilidade;             // 0-100
    private int impacto;                 // 0-100
    private int prioridadeSugerida;      // 1-5
    private String justificativa;
    private String modelo;               // ex.: gemini-3.6-flash ou heuristica-local
    private Instant avaliadoEm;
}
