package br.com.fiap.inovagab.api.controller;

import br.com.fiap.inovagab.api.domain.Recompensa;
import br.com.fiap.inovagab.api.domain.Resgate;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.ResgateRequest;
import br.com.fiap.inovagab.api.dto.ResgateResponse;
import br.com.fiap.inovagab.api.service.RecompensaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "6. Gamificacao", description = "Catalogo de recompensas e resgates com pontos (operador)")
public class RecompensaController {

    private final RecompensaService service;

    public RecompensaController(RecompensaService service) {
        this.service = service;
    }

    @Operation(summary = "Catalogo de recompensas disponiveis (todos)")
    @GetMapping("/recompensas")
    public List<Recompensa> recompensas() {
        return service.listar();
    }

    @Operation(summary = "Extrato dos meus resgates (OPERADOR)")
    @GetMapping("/resgates/meus")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<Resgate> meus(@AuthenticationPrincipal Usuario operador) {
        return service.meusResgates(operador);
    }

    @Operation(summary = "Resgatar recompensa - debito atomico de pontos (OPERADOR)")
    @PostMapping("/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    public ResgateResponse resgatar(@Valid @RequestBody ResgateRequest req, @AuthenticationPrincipal Usuario operador) {
        return service.resgatar(req.recompensaId(), operador);
    }
}
