package br.com.fiap.inovagab.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ResgateRequest(@NotBlank(message = "recompensaId e obrigatorio") String recompensaId) {
}
