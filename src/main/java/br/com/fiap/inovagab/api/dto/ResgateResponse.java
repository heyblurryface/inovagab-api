package br.com.fiap.inovagab.api.dto;

import br.com.fiap.inovagab.api.domain.Resgate;

public record ResgateResponse(Resgate resgate, int saldoAtual) {
}
