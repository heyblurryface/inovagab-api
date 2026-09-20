package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.IdeiaRequest;
import br.com.fiap.inovagab.api.dto.PrioridadeRequest;
import br.com.fiap.inovagab.api.dto.StatusIdeiaRequest;
import br.com.fiap.inovagab.api.service.IdeiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/ideias")
@Tag(name = "3. Ideias de inovacao", description = "Operador: CRUD das proprias ideias | Gestor: consulta, prioriza, aprova e usa IA")
public class IdeiaController {

    private final IdeiaService service;

    public IdeiaController(IdeiaService service) {
        this.service = service;
    }

    // ---------- operador ----------

    @Operation(summary = "Minhas ideias (OPERADOR)")
    @GetMapping("/minhas")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Ideia> minhas(@AuthenticationPrincipal Usuario autor) {
        return service.listarMinhas(autor);
    }

    @Operation(summary = "Cadastrar ideia - credita +5 pontos (OPERADOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    public Ideia criar(@Valid @RequestBody IdeiaRequest req, @AuthenticationPrincipal Usuario autor) {
        return service.criar(req, autor);
    }

    @Operation(summary = "Editar a propria ideia enquanto pendente (OPERADOR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public Ideia atualizar(@PathVariable String id, @Valid @RequestBody IdeiaRequest req,
                           @AuthenticationPrincipal Usuario autor) {
        return service.atualizar(id, req, autor);
    }

    @Operation(summary = "Excluir a propria ideia enquanto pendente (OPERADOR)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    public void excluir(@PathVariable String id, @AuthenticationPrincipal Usuario autor) {
        service.excluir(id, autor);
    }

    // ---------- gestor / lider ----------

    @Operation(summary = "Listar todas as ideias, com filtros opcionais ?status=pendente&orientacaoId=... (GESTOR, LIDER)")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','LIDER')")
    public List<Ideia> listar(@RequestParam(required = false) StatusIdeia status,
                              @RequestParam(required = false) String orientacaoId) {
        return service.listar(status, orientacaoId);
    }

    @Operation(summary = "Ideias aprovadas ordenadas por prioridade (GESTOR, LIDER)")
    @GetMapping("/aprovadas")
    @PreAuthorize("hasAnyRole('GESTOR','LIDER')")
    public List<Ideia> aprovadas() {
        return service.listarAprovadasPorPrioridade();
    }

    @Operation(summary = "Ranking das ideias avaliadas pela IA (GESTOR, LIDER)")
    @GetMapping("/ranking-ia")
    @PreAuthorize("hasAnyRole('GESTOR','LIDER')")
    public List<Ideia> ranking() {
        return service.ranking();
    }

    @Operation(summary = "Buscar ideia por id (operador so ve as proprias)")
    @GetMapping("/{id}")
    public Ideia buscar(@PathVariable String id, @AuthenticationPrincipal Usuario usuario) {
        return service.buscar(id, usuario);
    }

    @Operation(summary = "Definir prioridade 0-5 (GESTOR)")
    @PatchMapping("/{id}/prioridade")
    @PreAuthorize("hasRole('GESTOR')")
    public Ideia prioridade(@PathVariable String id, @Valid @RequestBody PrioridadeRequest req) {
        return service.definirPrioridade(id, req.prioridade());
    }

    @Operation(summary = "Aprovar/rejeitar - primeira aprovacao credita +50 pontos ao autor (GESTOR)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('GESTOR')")
    public Ideia status(@PathVariable String id, @Valid @RequestBody StatusIdeiaRequest req) {
        return service.definirStatus(id, req.status());
    }

    // ---------- IA (plus) ----------

    @Operation(summary = "[IA] Pontuar e priorizar uma ideia com Gemini (GESTOR). ?aplicarPrioridade=false so sugere")
    @PostMapping("/{id}/avaliar-ia")
    @PreAuthorize("hasRole('GESTOR')")
    public Ideia avaliarIa(@PathVariable String id,
                           @RequestParam(defaultValue = "true") boolean aplicarPrioridade) {
        return service.avaliarComIa(id, aplicarPrioridade);
    }

    @Operation(summary = "[IA] Avaliar em lote as ideias pendentes sem avaliacao generativa (GESTOR)")
    @PostMapping("/avaliar-ia")
    @PreAuthorize("hasRole('GESTOR')")
    public List<Ideia> avaliarPendentes(@RequestParam(defaultValue = "true") boolean aplicarPrioridade) {
        return service.avaliarPendentesComIa(aplicarPrioridade);
    }
}
