package br.com.fiap.inovagab.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeiaRequest(
        @NotBlank(message = "Titulo e obrigatorio") @Size(max = 120) String titulo,
        @NotBlank(message = "Descricao e obrigatoria") @Size(max = 3000) String descricao,
        @NotBlank(message = "Pilar e obrigatorio") String pilar,
        String orientacaoId
) {
}
