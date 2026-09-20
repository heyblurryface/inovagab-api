package br.com.fiap.inovagab.api.dto;

import br.com.fiap.inovagab.api.domain.Projeto;

public record ResumoProjetoResponse(
        String id,
        String titulo,
        String pilar,
        String orientacaoId,
        String orientacaoTitulo,
        String etapa,
        String status,
        double investimento,
        double retornoFinanceiro,
        double lucro,
        double roi,
        int prazoMeses,
        double aumentoProdutividade,
        double reducaoCustos
) {
    public static ResumoProjetoResponse de(Projeto p) {
        return new ResumoProjetoResponse(
                p.getId(), p.getTitulo(), p.getPilar(), p.getOrientacaoId(), p.getOrientacaoTitulo(),
                p.getEtapa() != null ? p.getEtapa().getValue() : null,
                p.getStatus() != null ? p.getStatus().getValue() : null,
                p.getInvestimento(), p.getRetornoFinanceiro(), p.getLucro(), p.getRoi(),
                p.getPrazoMeses(), p.getAumentoProdutividade(), p.getReducaoCustos());
    }
}
