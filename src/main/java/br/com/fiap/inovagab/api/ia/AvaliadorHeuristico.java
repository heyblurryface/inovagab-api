package br.com.fiap.inovagab.api.ia;

import br.com.fiap.inovagab.api.domain.AvaliacaoIa;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Fallback local (sem chamada externa) usado quando a Gemini API nao esta configurada ou falha.
 * A avaliacao fica claramente identificada com modelo = "heuristica-local".
 */
public final class AvaliadorHeuristico {

    public static final String MODELO = "heuristica-local";

    private static final List<String> PALAVRAS_IMPACTO = List.of(
            "custo", "economia", "reduz", "tempo", "produtividade", "seguranca", "cliente",
            "receita", "automat", "retrabalho", "desperdicio", "atraso", "combustivel", "manutencao",
            "qualidade", "digital", "app", "dados", "processo");

    private AvaliadorHeuristico() {
    }

    public static AvaliacaoIa avaliar(Ideia ideia, List<Orientacao> orientacoesVigentes) {
        return avaliar(ideia, orientacoesVigentes, null);
    }

    /**
     * @param motivoFallback por que a IA generativa nao foi usada nesta avaliacao.
     *                       Vai para a justificativa, para o gestor saber o que aconteceu.
     */
    public static AvaliacaoIa avaliar(Ideia ideia, List<Orientacao> orientacoesVigentes, String motivoFallback) {
        String texto = (ideia.getTitulo() + " " + ideia.getDescricao()).toLowerCase(Locale.ROOT);

        boolean vinculada = ideia.getOrientacaoId() != null && !ideia.getOrientacaoId().isBlank();
        boolean pilarAlinhado = orientacoesVigentes.stream()
                .anyMatch(o -> o.getPilar() != null && o.getPilar().equalsIgnoreCase(ideia.getPilar()));
        int alinhamento = vinculada ? 85 : (pilarAlinhado ? 65 : 35);

        int tamanho = ideia.getDescricao() != null ? ideia.getDescricao().length() : 0;
        int viabilidade = clamp(40 + tamanho / 15, 30, 90);

        long termos = PALAVRAS_IMPACTO.stream().filter(texto::contains).count();
        int impacto = clamp(30 + (int) termos * 12, 30, 95);

        int pontuacao = clamp((int) Math.round(alinhamento * 0.4 + viabilidade * 0.3 + impacto * 0.3), 0, 100);
        int prioridade = clamp((int) Math.ceil(pontuacao / 20.0), 1, 5);

        String justificativa = String.format(Locale.ROOT,
                "Avaliacao heuristica local: %s a estrategia vigente (%d), descricao com %d caracteres (viabilidade %d) "
                        + "e %d termo(s) de impacto identificados (impacto %d).",
                vinculada ? "vinculada" : (pilarAlinhado ? "pilar alinhado" : "sem vinculo claro"),
                alinhamento, tamanho, viabilidade, termos, impacto);

        justificativa += (motivoFallback == null || motivoFallback.isBlank())
                ? " Configure GEMINI_API_KEY para avaliacao com IA generativa."
                : " IA generativa nao utilizada nesta avaliacao: " + resumir(motivoFallback);

        return AvaliacaoIa.builder()
                .pontuacao(pontuacao)
                .alinhamentoEstrategico(alinhamento)
                .viabilidade(viabilidade)
                .impacto(impacto)
                .prioridadeSugerida(prioridade)
                .justificativa(justificativa)
                .modelo(MODELO)
                .avaliadoEm(Instant.now())
                .build();
    }

    /** Deixa o motivo curto o suficiente para caber na justificativa exibida no app. */
    private static String resumir(String motivo) {
        String limpo = motivo.replaceAll("\\s+", " ").trim();
        return limpo.length() > 180 ? limpo.substring(0, 177) + "..." : limpo;
    }

    static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
