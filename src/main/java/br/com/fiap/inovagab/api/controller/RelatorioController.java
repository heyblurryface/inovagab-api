package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.dto.ResumoEstrategiaResponse;
import br.com.fiap.inovagab.api.dto.ResumoGeralResponse;
import br.com.fiap.inovagab.api.dto.ResumoPilarResponse;
import br.com.fiap.inovagab.api.dto.ResumoProjetoResponse;
import br.com.fiap.inovagab.api.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "5. Relatorios / Dashboard", description = "Resumos estruturados para a lideranca (graficos no app)")
@PreAuthorize("hasAnyRole('LIDER','GESTOR')")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @Operation(summary = "Resumo geral - ROI, lucro, investimento, prazo, produtividade, contagens (LIDER, GESTOR)")
    @GetMapping("/resumo")
    public ResumoGeralResponse resumo() {
        return service.resumoGeral();
    }

    @Operation(summary = "Resultados agrupados por estrategia (LIDER, GESTOR)")
    @GetMapping("/por-estrategia")
    public List<ResumoEstrategiaResponse> porEstrategia() {
        return service.porEstrategia();
    }

    @Operation(summary = "Resultados de uma estrategia especifica (LIDER, GESTOR)")
    @GetMapping("/por-estrategia/{orientacaoId}")
    public ResumoEstrategiaResponse porEstrategia(@PathVariable String orientacaoId) {
        return service.porEstrategia(orientacaoId);
    }

    @Operation(summary = "Resultados projeto a projeto (LIDER, GESTOR)")
    @GetMapping("/por-projeto")
    public List<ResumoProjetoResponse> porProjeto() {
        return service.porProjeto();
    }

    @Operation(summary = "Resultados agrupados por pilar de inovacao (LIDER, GESTOR)")
    @GetMapping("/por-pilar")
    public List<ResumoPilarResponse> porPilar() {
        return service.porPilar();
    }
}
