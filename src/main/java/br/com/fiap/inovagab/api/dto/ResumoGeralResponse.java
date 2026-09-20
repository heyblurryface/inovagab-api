package br.com.fiap.inovagab.api.dto;

import java.util.Map;

public record ResumoGeralResponse(
        ResumoFinanceiro kpis,
        Map<String, Long> projetosPorStatus,
        Map<String, Long> projetosPorEtapa,
        long totalIdeias,
        long ideiasPendentes,
        long ideiasAprovadas,
        long ideiasRejeitadas,
        long estrategiasAtivas
) {
}
