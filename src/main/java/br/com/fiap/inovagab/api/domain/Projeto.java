package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Projeto / iniciativa de inovacao (gerenciado pelos gestores, consultado pelos lideres). */
@Document("projetos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String pilar;
    private String orientacaoId;      // vinculo com a estrategia vigente
    private String orientacaoTitulo;
    private String ideiaOrigemId;
    private String ideiaOrigemTitulo;
    private String gestorId;
    private String gestorNome;
    private EtapaProjeto etapa;
    private StatusProjeto status;
    private double investimento;
    private double retornoFinanceiro;
    private int prazoMeses;
    private double aumentoProdutividade; // %
    private double reducaoCustos;        // %
    private String resultados;           // descricao dos resultados obtidos
    private Instant criadoEm;
    private Instant atualizadoEm;

    /** ROI em % - calculado, nao persistido. */
    @JsonProperty("roi")
    public double getRoi() {
        return investimento > 0 ? ((retornoFinanceiro - investimento) / investimento) * 100 : 0.0;
    }

    /** Lucro (retorno - investimento) - calculado, nao persistido. */
    @JsonProperty("lucro")
    public double getLucro() {
        return retornoFinanceiro - investimento;
    }
}
