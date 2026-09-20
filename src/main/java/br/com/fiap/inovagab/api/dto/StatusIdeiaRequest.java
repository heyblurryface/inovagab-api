package br.com.fiap.inovagab.api.dto;

import br.com.fiap.inovagab.api.domain.StatusIdeia;
import jakarta.validation.constraints.NotNull;

public record StatusIdeiaRequest(@NotNull(message = "Status e obrigatorio") StatusIdeia status) {
}
