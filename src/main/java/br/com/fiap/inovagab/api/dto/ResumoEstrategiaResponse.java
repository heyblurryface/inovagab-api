package br.com.fiap.inovagab.api.dto;

public record ResumoEstrategiaResponse(
        String orientacaoId,
        String titulo,
        String pilar,
        String categoria,
        String campanha,
        boolean ativa,
        long quantidadeIdeias,
        long ideiasAprovadas,
        ResumoFinanceiro kpis
) {
}
