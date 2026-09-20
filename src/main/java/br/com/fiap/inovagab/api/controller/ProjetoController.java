package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.ProjetoRequest;
import br.com.fiap.inovagab.api.service.ProjetoService;
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
@RequestMapping("/api/projetos")
@Tag(name = "4. Projetos e iniciativas", description = "Gestor: CRUD e acompanhamento | Lider: consulta")
@PreAuthorize("hasAnyRole('GESTOR','LIDER')")
public class ProjetoController {

    private final ProjetoService service;

    public ProjetoController(ProjetoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar projetos, com filtros opcionais ?status=em_andamento&orientacaoId=... (GESTOR, LIDER)")
    @GetMapping
    public List<Projeto> listar(@RequestParam(required = false) StatusProjeto status,
                                @RequestParam(required = false) String orientacaoId) {
        return service.listar(status, orientacaoId);
    }

    @Operation(summary = "Buscar projeto por id (GESTOR, LIDER)")
    @GetMapping("/{id}")
    public Projeto buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @Operation(summary = "Cadastrar projeto/iniciativa (GESTOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    public Projeto criar(@Valid @RequestBody ProjetoRequest req, @AuthenticationPrincipal Usuario gestor) {
        return service.criar(req, gestor);
    }

    @Operation(summary = "Atualizar projeto - etapa, status, investimento, prazo, retorno, resultados (GESTOR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public Projeto atualizar(@PathVariable String id, @Valid @RequestBody ProjetoRequest req) {
        return service.atualizar(id, req);
    }

    @Operation(summary = "Excluir projeto nao concluido (GESTOR)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GESTOR')")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
