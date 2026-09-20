package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.AvaliacaoIa;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.ia.AvaliadorHeuristico;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliadorHeuristicoTest {

    @Test
    void ideiaVinculadaAEstrategiaPontuaMaisQueSemVinculo() {
        Orientacao o = Orientacao.builder().id("o1").pilar("Mensuracao").build();
        Ideia vinculada = Ideia.builder().titulo("Telemetria").descricao("Reduz custo de combustivel e tempo parado")
                .pilar("Mensuracao").orientacaoId("o1").build();
        Ideia solta = Ideia.builder().titulo("Telemetria").descricao("Reduz custo de combustivel e tempo parado")
                .pilar("Outro").build();

        AvaliacaoIa a = AvaliadorHeuristico.avaliar(vinculada, List.of(o));
        AvaliacaoIa b = AvaliadorHeuristico.avaliar(solta, List.of(o));

        assertTrue(a.getPontuacao() > b.getPontuacao());
        assertEquals(AvaliadorHeuristico.MODELO, a.getModelo());
        assertTrue(a.getPrioridadeSugerida() >= 1 && a.getPrioridadeSugerida() <= 5);
    }
}
