package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Registro historico de uma orientacao estrategica (id, data, categoria, campanha). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHistorico {
    private String id;
    private Instant data;
    private String acao;        // CRIADA | ATUALIZADA | ENCERRADA | REATIVADA
    private String categoria;
    private String campanha;
    private String usuarioNome;
    private String detalhe;
}
