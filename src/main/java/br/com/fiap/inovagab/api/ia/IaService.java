package br.com.fiap.inovagab.api.ia;

import br.com.fiap.inovagab.api.config.IaProperties;
import br.com.fiap.inovagab.api.domain.AvaliacaoIa;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.exception.IaIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Diferencial de IA (Plus): pontuacao e priorizacao automatica das ideias de inovacao.
 * Usa Google Gemini (API gratuita) com prompt estruturado e resposta em JSON.
 */
@Slf4j
@Service
public class IaService {

    private final IaProperties props;
    private final GeminiClient gemini;

    public IaService(IaProperties props, GeminiClient gemini) {
        this.props = props;
        this.gemini = gemini;
    }

    public boolean geminiConfigurado() {
        return props.geminiConfigurado();
    }

    public AvaliacaoIa avaliar(Ideia ideia, List<Orientacao> orientacoesVigentes) {
        if (!props.geminiConfigurado()) {
            if (props.fallbackHeuristica()) {
                log.info("GEMINI_API_KEY nao configurada - usando avaliacao heuristica local para ideia {}", ideia.getId());
                return AvaliadorHeuristico.avaliar(ideia, orientacoesVigentes);
            }
            throw new IaIndisponivelException("IA nao configurada. Defina a variavel GEMINI_API_KEY.", null);
        }

        try {
            GeminiClient.Resultado resultado = gemini.gerarJson(montarPrompt(ideia, orientacoesVigentes));
            JsonNode json = resultado.json();
            return AvaliacaoIa.builder()
                    .pontuacao(nota(json, "pontuacao", 0, 100))
                    .alinhamentoEstrategico(nota(json, "alinhamentoEstrategico", 0, 100))
                    .viabilidade(nota(json, "viabilidade", 0, 100))
                    .impacto(nota(json, "impacto", 0, 100))
                    .prioridadeSugerida(nota(json, "prioridadeSugerida", 1, 5))
                    .justificativa(json.path("justificativa").asText("Sem justificativa retornada."))
                    .modelo(resultado.modelo())
                    .avaliadoEm(Instant.now())
                    .build();
        } catch (Exception e) {
            if (props.fallbackHeuristica()) {
                log.warn("Falha na Gemini API ({}). Usando heuristica local para ideia {}", e.getMessage(), ideia.getId());
                return AvaliadorHeuristico.avaliar(ideia, orientacoesVigentes, motivo(e));
            }
            throw new IaIndisponivelException("Falha ao consultar a IA: " + e.getMessage(), e);
        }
    }

    /** Transforma a excecao em um texto curto que explica a falha para quem esta usando o app. */
    private static String motivo(Exception e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (msg.contains("PerDay")) {
            return "cota diaria gratuita da Gemini esgotada para o(s) modelo(s) configurado(s) (429)";
        }
        if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED")) {
            return "limite de requisicoes da Gemini atingido (429) - tente novamente em alguns minutos";
        }
        if (msg.contains("503") || msg.contains("UNAVAILABLE")) {
            return "modelo sobrecarregado no momento (503)";
        }
        if (msg.contains("403") || msg.contains("PERMISSION_DENIED")) {
            return "chave da Gemini sem permissao (403)";
        }
        if (msg.contains("404")) {
            return "modelo nao encontrado (404) - verifique GEMINI_MODEL";
        }
        return msg;
    }

    private static int nota(JsonNode json, String campo, int min, int max) {
        return AvaliadorHeuristico.clamp(json.path(campo).asInt(min), min, max);
    }

    private String montarPrompt(Ideia ideia, List<Orientacao> orientacoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                Voce e um analista de inovacao do Grupo Aguia Branca (transporte e logistica no Brasil).
                Sua tarefa e pontuar e priorizar uma ideia de inovacao enviada por um colaborador operacional,
                considerando as orientacoes estrategicas vigentes da empresa.

                ORIENTACOES ESTRATEGICAS VIGENTES:
                """);
        if (orientacoes.isEmpty()) {
            sb.append("- (nenhuma orientacao cadastrada)\n");
        }
        for (Orientacao o : orientacoes) {
            sb.append("- [").append(o.getId()).append("] ").append(o.getTitulo())
                    .append(" | pilar: ").append(o.getPilar())
                    .append(" | categoria: ").append(o.getCategoria())
                    .append(" | campanha: ").append(o.getCampanha() != null ? o.getCampanha() : "-")
                    .append("\n  ").append(resumir(o.getDescricao(), 300)).append("\n");
        }

        sb.append("\nIDEIA A AVALIAR:\n")
                .append("Titulo: ").append(ideia.getTitulo()).append("\n")
                .append("Pilar informado: ").append(ideia.getPilar()).append("\n")
                .append("Orientacao vinculada pelo autor: ")
                .append(ideia.getOrientacaoId() != null ? ideia.getOrientacaoId() : "nenhuma").append("\n")
                .append("Descricao: ").append(resumir(ideia.getDescricao(), 2500)).append("\n");

        sb.append("""

                CRITERIOS (0 a 100 cada):
                - alinhamentoEstrategico: aderencia as orientacoes vigentes (pilar, categoria, campanha).
                - viabilidade: clareza, esforco/custo estimado e facilidade de implementar na operacao.
                - impacto: potencial de resultado (reducao de custos, produtividade, seguranca, experiencia do cliente).
                - pontuacao: nota final ponderada (alinhamento 40%, viabilidade 30%, impacto 30%).
                - prioridadeSugerida: inteiro de 1 (baixa) a 5 (maxima).
                - justificativa: texto curto (ate 3 frases) em portugues explicando a nota para o gestor.

                Responda SOMENTE com um JSON valido neste formato, sem markdown:
                {"pontuacao":0,"alinhamentoEstrategico":0,"viabilidade":0,"impacto":0,"prioridadeSugerida":1,"justificativa":""}
                """);
        return sb.toString();
    }

    private static String resumir(String texto, int max) {
        if (texto == null) return "";
        return texto.length() <= max ? texto : texto.substring(0, max) + "...";
    }
}
