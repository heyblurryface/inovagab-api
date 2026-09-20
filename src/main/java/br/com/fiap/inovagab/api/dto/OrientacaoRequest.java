package br.com.fiap.inovagab.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OrientacaoRequest(
        @NotBlank(message = "Titulo e obrigatorio") @Size(max = 120) String titulo,
        @NotBlank(message = "Descricao e obrigatoria") @Size(max = 2000) String descricao,
        @NotBlank(message = "Pilar e obrigatorio") String pilar,
        @NotBlank(message = "Categoria e obrigatoria") String categoria,
        String campanha,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativa
) {
}
