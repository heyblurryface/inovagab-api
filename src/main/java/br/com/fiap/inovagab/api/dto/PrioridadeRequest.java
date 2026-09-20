package br.com.fiap.inovagab.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PrioridadeRequest(
        @Min(value = 0, message = "Prioridade minima e 0") @Max(value = 5, message = "Prioridade maxima e 5") int prioridade
) {
}
