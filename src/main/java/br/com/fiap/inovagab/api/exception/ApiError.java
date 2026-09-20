package br.com.fiap.inovagab.api.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        Map<String, String> campos
) {
}
