package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Orientacao estrategica da empresa (gerenciada pela lideranca). */
@Document("orientacoes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orientacao {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String pilar;
    private String categoria;
    private String campanha;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean ativa;
    private String criadoPorId;
    private String criadoPorNome;
    private Instant criadoEm;
    private Instant atualizadoEm;

    @Builder.Default
    private List<RegistroHistorico> historico = new ArrayList<>();
}
