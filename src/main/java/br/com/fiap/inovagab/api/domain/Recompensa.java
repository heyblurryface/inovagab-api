package br.com.fiap.inovagab.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {
    @Id
    private String id;
    private String titulo;
    private String descricao;
    private int custo;
    private String emoji;
    private String categoria;
    private boolean disponivel;
}
