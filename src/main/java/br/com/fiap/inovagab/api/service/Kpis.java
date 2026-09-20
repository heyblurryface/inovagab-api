package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import br.com.fiap.inovagab.api.dto.ResumoFinanceiro;

import java.util.List;

/** Calculo dos KPIs do portfolio (regra de negocio pura, testavel sem Spring). */
public final class Kpis {

    private Kpis() {
    }

    public static ResumoFinanceiro calcular(List<Projeto> projetos) {
        if (projetos == null || projetos.isEmpty()) {
            return new ResumoFinanceiro(0, 0, 0, 0, 0, 0, 0, 0);
        }

        double invest = projetos.stream().mapToDouble(Projeto::getInvestimento).sum();
        double retorno = projetos.stream().mapToDouble(Projeto::getRetornoFinanceiro).sum();
        double lucro = retorno - invest;
        double roi = invest > 0 ? (lucro / invest) * 100 : 0.0;

        List<Projeto> ativos = projetos.stream()
                .filter(p -> p.getStatus() != StatusProjeto.CANCELADO)
                .toList();

        double prazoMedio = ativos.stream().mapToInt(Projeto::getPrazoMeses).average().orElse(0);
        double produtividade = ativos.stream().mapToDouble(Projeto::getAumentoProdutividade).average().orElse(0);
        double reducaoCustos = ativos.stream().mapToDouble(Projeto::getReducaoCustos).average().orElse(0);

        return new ResumoFinanceiro(projetos.size(), arredondar(invest), arredondar(retorno), arredondar(lucro),
                arredondar(roi), arredondar(prazoMedio), arredondar(produtividade), arredondar(reducaoCustos));
    }

    static double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
