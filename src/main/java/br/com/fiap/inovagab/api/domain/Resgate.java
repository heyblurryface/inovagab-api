package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("resgates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resgate {
    @Id
    private String id;
    private String recompensaId;
    private String recompensaTitulo;
    private String recompensaEmoji;
    private int custoPago;
    private String operadorId;
    private String operadorNome;
    private Instant resgatadoEm;
    private String status;
}
