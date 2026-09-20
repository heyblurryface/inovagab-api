package br.com.fiap.inovagab.api.dto;

/** Bloco de KPIs reutilizado pelo resumo geral, por estrategia e por pilar. */
public record ResumoFinanceiro(
        int quantidadeProjetos,
        double investimentoTotal,
        double retornoTotal,
        double lucroTotal,
        double roi,
        double prazoMedioMeses,
        double produtividadeMedia,
        double reducaoCustosMedia
) {
}
