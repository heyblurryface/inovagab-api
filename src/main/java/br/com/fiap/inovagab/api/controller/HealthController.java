package br.com.fiap.inovagab.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@Tag(name = "0. Health", description = "Verificacao de disponibilidade (publico)")
public class HealthController {

    @Operation(summary = "Status da API")
    @GetMapping({"/", "/api/health"})
    public Map<String, Object> health() {
        return Map.of(
                "aplicacao", "InovaGAB API",
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "docs", "/swagger-ui.html");
    }
}
