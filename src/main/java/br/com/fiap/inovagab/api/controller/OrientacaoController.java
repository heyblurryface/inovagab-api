package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.RegistroHistorico;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.OrientacaoRequest;
import br.com.fiap.inovagab.api.service.OrientacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orientacoes")
@Tag(name = "2. Orientacoes estrategicas", description = "CRUD pela lideranca; consulta por todos os perfis")
public class OrientacaoController {

    private final OrientacaoService service;

    public OrientacaoController(OrientacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar orientacoes vigentes (todos). Lider pode incluir inativas com ?incluirInativas=true")
    @GetMapping
    public List<Orientacao> listar(@RequestParam(defaultValue = "false") boolean incluirInativas,
                                   @AuthenticationPrincipal Usuario usuario) {
        return service.listar(incluirInativas, usuario);
    }

    @Operation(summary = "Buscar orientacao por id (todos)")
    @GetMapping("/{id}")
    public Orientacao buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @Operation(summary = "Historico da orientacao - id, data, categoria, campanha (todos)")
    @GetMapping("/{id}/historico")
    public List<RegistroHistorico> historico(@PathVariable String id) {
        return service.historico(id);
    }

    @Operation(summary = "Criar orientacao (LIDER)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIDER')")
    public Orientacao criar(@Valid @RequestBody OrientacaoRequest req, @AuthenticationPrincipal Usuario lider) {
        return service.criar(req, lider);
    }

    @Operation(summary = "Atualizar orientacao (LIDER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public Orientacao atualizar(@PathVariable String id, @Valid @RequestBody OrientacaoRequest req,
                                @AuthenticationPrincipal Usuario lider) {
        return service.atualizar(id, req, lider);
    }

    @Operation(summary = "Encerrar orientacao - exclusao logica, preserva historico (LIDER)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public Orientacao encerrar(@PathVariable String id, @AuthenticationPrincipal Usuario lider) {
        return service.encerrar(id, lider);
    }
}
