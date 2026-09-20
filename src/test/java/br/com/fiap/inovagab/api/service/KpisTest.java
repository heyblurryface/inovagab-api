package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import br.com.fiap.inovagab.api.dto.ResumoFinanceiro;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KpisTest {

    @Test
    void listaVaziaRetornaZeros() {
        ResumoFinanceiro r = Kpis.calcular(List.of());
        assertEquals(0, r.quantidadeProjetos());
        assertEquals(0.0, r.roi());
    }

    @Test
    void calculaRoiLucroEMediasIgnorandoCancelados() {
        Projeto a = Projeto.builder().investimento(100).retornoFinanceiro(150).prazoMeses(6)
                .aumentoProdutividade(10).reducaoCustos(4).status(StatusProjeto.CONCLUIDO).build();
        Projeto b = Projeto.builder().investimento(100).retornoFinanceiro(100).prazoMeses(12)
                .aumentoProdutividade(20).reducaoCustos(8).status(StatusProjeto.EM_ANDAMENTO).build();
        Projeto cancelado = Projeto.builder().investimento(50).retornoFinanceiro(0).prazoMeses(99)
                .aumentoProdutividade(99).reducaoCustos(99).status(StatusProjeto.CANCELADO).build();

        ResumoFinanceiro r = Kpis.calcular(List.of(a, b, cancelado));

        assertEquals(3, r.quantidadeProjetos());
        assertEquals(250.0, r.investimentoTotal());
        assertEquals(250.0, r.retornoTotal());
        assertEquals(0.0, r.lucroTotal());
        assertEquals(0.0, r.roi());
        assertEquals(9.0, r.prazoMedioMeses());       // media apenas dos nao cancelados
        assertEquals(15.0, r.produtividadeMedia());
        assertEquals(6.0, r.reducaoCustosMedia());
    }

    @Test
    void roiDoProjetoIndividual() {
        Projeto p = Projeto.builder().investimento(200).retornoFinanceiro(300).build();
        assertEquals(50.0, p.getRoi());
        assertEquals(100.0, p.getLucro());
    }
}
