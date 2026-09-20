package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import br.com.fiap.inovagab.api.dto.ResumoEstrategiaResponse;
import br.com.fiap.inovagab.api.dto.ResumoGeralResponse;
import br.com.fiap.inovagab.api.dto.ResumoPilarResponse;
import br.com.fiap.inovagab.api.dto.ResumoProjetoResponse;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.repository.IdeiaRepository;
import br.com.fiap.inovagab.api.repository.OrientacaoRepository;
import br.com.fiap.inovagab.api.repository.ProjetoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Endpoints de relatorio: dados resumidos para o dashboard da lideranca. */
@Service
public class RelatorioService {

    private final ProjetoRepository projetoRepository;
    private final IdeiaRepository ideiaRepository;
    private final OrientacaoRepository orientacaoRepository;

    public RelatorioService(ProjetoRepository projetoRepository, IdeiaRepository ideiaRepository,
                            OrientacaoRepository orientacaoRepository) {
        this.projetoRepository = projetoRepository;
        this.ideiaRepository = ideiaRepository;
        this.orientacaoRepository = orientacaoRepository;
    }

    public ResumoGeralResponse resumoGeral() {
        List<Projeto> projetos = projetoRepository.findAll();

        Map<String, Long> porStatus = new LinkedHashMap<>();
        Map<String, Long> porEtapa = new LinkedHashMap<>();
        for (Projeto p : projetos) {
            String status = p.getStatus() != null ? p.getStatus().getValue() : "indefinido";
            String etapa = p.getEtapa() != null ? p.getEtapa().getValue() : "indefinida";
            porStatus.merge(status, 1L, Long::sum);
            porEtapa.merge(etapa, 1L, Long::sum);
        }

        return new ResumoGeralResponse(
                Kpis.calcular(projetos),
                porStatus,
                porEtapa,
                ideiaRepository.count(),
                ideiaRepository.countByStatus(StatusIdeia.PENDENTE),
                ideiaRepository.countByStatus(StatusIdeia.APROVADA),
                ideiaRepository.countByStatus(StatusIdeia.REJEITADA),
                orientacaoRepository.findByAtivaTrueOrderByCriadoEmDesc().size());
    }

    public List<ResumoEstrategiaResponse> porEstrategia() {
        List<Orientacao> orientacoes = orientacaoRepository.findAllByOrderByCriadoEmDesc();
        List<Projeto> projetos = projetoRepository.findAll();
        List<Ideia> ideias = ideiaRepository.findAll();

        List<ResumoEstrategiaResponse> resultado = new ArrayList<>();
        for (Orientacao o : orientacoes) {
            List<Projeto> doGrupo = projetos.stream().filter(p -> o.getId().equals(p.getOrientacaoId())).toList();
            List<Ideia> ideiasDoGrupo = ideias.stream().filter(i -> o.getId().equals(i.getOrientacaoId())).toList();
            resultado.add(new ResumoEstrategiaResponse(
                    o.getId(), o.getTitulo(), o.getPilar(), o.getCategoria(), o.getCampanha(), o.isAtiva(),
                    ideiasDoGrupo.size(),
                    ideiasDoGrupo.stream().filter(i -> i.getStatus() == StatusIdeia.APROVADA).count(),
                    Kpis.calcular(doGrupo)));
        }

        // Projetos sem estrategia vinculada
        List<Projeto> semEstrategia = projetos.stream()
                .filter(p -> p.getOrientacaoId() == null || p.getOrientacaoId().isBlank()).toList();
        if (!semEstrategia.isEmpty()) {
            long ideiasSem = ideias.stream().filter(i -> i.getOrientacaoId() == null || i.getOrientacaoId().isBlank()).count();
            resultado.add(new ResumoEstrategiaResponse(null, "Sem estrategia vinculada", null, null, null, false,
                    ideiasSem, 0, Kpis.calcular(semEstrategia)));
        }
        return resultado;
    }

    public ResumoEstrategiaResponse porEstrategia(String orientacaoId) {
        return porEstrategia().stream()
                .filter(r -> orientacaoId.equals(r.orientacaoId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orientacao nao encontrada: " + orientacaoId));
    }

    public List<ResumoProjetoResponse> porProjeto() {
        return projetoRepository.findAllByOrderByCriadoEmDesc().stream()
                .map(ResumoProjetoResponse::de)
                .toList();
    }

    public List<ResumoPilarResponse> porPilar() {
        List<Projeto> projetos = projetoRepository.findAll();
        Map<String, Long> ideiasPorPilar = ideiaRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getPilar() != null ? i.getPilar() : "-", Collectors.counting()));

        Map<String, List<Projeto>> grupos = projetos.stream()
                .collect(Collectors.groupingBy(p -> p.getPilar() != null ? p.getPilar() : "-", LinkedHashMap::new, Collectors.toList()));

        List<ResumoPilarResponse> resultado = new ArrayList<>();
        grupos.forEach((pilar, lista) ->
                resultado.add(new ResumoPilarResponse(pilar, ideiasPorPilar.getOrDefault(pilar, 0L), Kpis.calcular(lista))));
        return resultado;
    }
}
