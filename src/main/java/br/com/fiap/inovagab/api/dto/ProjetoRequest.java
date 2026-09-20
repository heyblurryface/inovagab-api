package br.com.fiap.inovagab.api.dto;

import br.com.fiap.inovagab.api.domain.EtapaProjeto;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProjetoRequest(
        @NotBlank(message = "Titulo e obrigatorio") @Size(max = 120) String titulo,
        @NotBlank(message = "Descricao e obrigatoria") @Size(max = 3000) String descricao,
        @NotBlank(message = "Pilar e obrigatorio") String pilar,
        String orientacaoId,
        String ideiaOrigemId,
        EtapaProjeto etapa,
        StatusProjeto status,
        @PositiveOrZero(message = "Investimento nao pode ser negativo") double investimento,
        @PositiveOrZero(message = "Retorno nao pode ser negativo") double retornoFinanceiro,
        @Min(value = 0, message = "Prazo nao pode ser negativo") int prazoMeses,
        double aumentoProdutividade,
        double reducaoCustos,
        @Size(max = 3000) String resultados
) {
}
