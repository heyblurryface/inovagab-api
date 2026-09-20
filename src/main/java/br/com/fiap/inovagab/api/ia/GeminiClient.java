package br.com.fiap.inovagab.api.ia;

import br.com.fiap.inovagab.api.config.IaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente da Google Gemini API (generateContent), usando RestClient do Spring 6.
 * Docs: https://ai.google.dev/api/generate-content
 *
 * <p>Tres camadas de resiliencia, nesta ordem:</p>
 * <ol>
 *   <li><b>Retry</b> para erros transitorios (503 por pico de demanda, 429 por limite por minuto),
 *       respeitando o {@code retryDelay} que a propria API sugere.</li>
 *   <li><b>Cascata de modelos</b>: a cota gratuita e contada por modelo, entao quando um esgota a
 *       cota diaria o cliente tenta o proximo da lista {@code GEMINI_MODEL}.</li>
 *   <li>Esgotadas as opcoes, o {@code IaService} usa o avaliador heuristico local e registra o
 *       motivo na justificativa.</li>
 * </ol>
 */
@Slf4j
@Component
public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    /** Tentativas por modelo (1 original + 2 repeticoes). */
    private static final int MAX_TENTATIVAS = 3;
    /** Espera base entre tentativas, quadruplicada a cada rodada: 3s e 12s. */
    private static final long ESPERA_BASE_MS = 3000L;
    /** Acima disso nao vale segurar a requisicao do usuario. */
    private static final long ESPERA_MAXIMA_MS = 25000L;

    /** Ex.: "retryDelay": "34s" no corpo do erro. */
    private static final Pattern RETRY_DELAY = Pattern.compile("\"retryDelay\"\\s*:\\s*\"(\\d+)(?:\\.\\d+)?s\"");

    private final IaProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiClient(IaProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    /** Modelo preferencial (o primeiro da lista configurada). */
    public String modelo() {
        return props.geminiModelos().get(0);
    }

    /** Resposta da IA junto com o modelo que de fato respondeu. */
    public record Resultado(JsonNode json, String modelo) {
    }

    /**
     * Envia o prompt pedindo resposta JSON, percorrendo os modelos configurados ate um responder.
     */
    public Resultado gerarJson(String prompt) throws Exception {
        List<String> modelos = props.geminiModelos();
        Exception ultimaFalha = null;

        for (String modelo : modelos) {
            try {
                return new Resultado(chamarComRetry(prompt, modelo), modelo);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                ultimaFalha = e;
                boolean temProximo = modelos.indexOf(modelo) < modelos.size() - 1;
                if (temProximo) {
                    log.warn("Modelo {} indisponivel ({}). Tentando o proximo da lista.",
                            modelo, e.getStatusCode());
                } 
            }
        }

        throw ultimaFalha != null ? ultimaFalha : new IllegalStateException("Nenhum modelo Gemini configurado");
    }

    private JsonNode chamarComRetry(String prompt, String modelo) throws Exception {
        HttpStatusCode ultimoStatus = null;
        RuntimeException ultimaFalha = null;

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                return chamar(prompt, modelo);
            } catch (HttpServerErrorException | HttpClientErrorException e) {
                ultimoStatus = e.getStatusCode();
                ultimaFalha = e;
                String corpo = e.getResponseBodyAsString();

                if (!ehTransitorio(e.getStatusCode()) || cotaDiariaEsgotada(corpo) || tentativa == MAX_TENTATIVAS) {
                    throw e;
                }

                long espera = esperaSugerida(corpo)
                        .orElse(ESPERA_BASE_MS * (long) Math.pow(4, tentativa - 1));
                if (espera > ESPERA_MAXIMA_MS) {
                    log.warn("Gemini pediu {} ms de espera, acima do limite aceitavel. Desistindo de {}.",
                            espera, modelo);
                    throw e;
                }
                log.warn("Gemini {} indisponivel ({}). Tentativa {}/{}, nova tentativa em {} ms",
                        modelo, e.getStatusCode(), tentativa, MAX_TENTATIVAS, espera);
                Thread.sleep(espera);
            }
        }

        throw ultimaFalha != null ? ultimaFalha
                : new IllegalStateException("Falha na Gemini API (" + ultimoStatus + ")");
    }

    /** 503 (sobrecarga) e 429 (limite de requisicoes) valem uma nova tentativa. */
    private static boolean ehTransitorio(HttpStatusCode status) {
        return status.value() == 503 || status.value() == 429;
    }

    /**
     * Cota diaria do plano gratuito: repetir daqui a alguns segundos nao resolve,
     * o certo e partir para o proximo modelo (a cota e contada por modelo).
     */
    private static boolean cotaDiariaEsgotada(String corpoDoErro) {
        return corpoDoErro != null && corpoDoErro.contains("PerDay");
    }

    /** Le o retryDelay que a Gemini devolve no corpo do erro e converte para milissegundos. */
    private static Optional<Long> esperaSugerida(String corpoDoErro) {
        if (corpoDoErro == null || corpoDoErro.isBlank()) {
            return Optional.empty();
        }
        Matcher m = RETRY_DELAY.matcher(corpoDoErro);
        if (m.find()) {
            // um segundo a mais de folga, para nao bater na virada da janela
            return Optional.of(Long.parseLong(m.group(1)) * 1000L + 1000L);
        }
        return Optional.empty();
    }

    private JsonNode chamar(String prompt, String modelo) throws Exception {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"));

        JsonNode resposta = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", modelo)
                .header("x-goog-api-key", props.geminiApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (resposta == null) {
            throw new IllegalStateException("Resposta vazia da Gemini API");
        }

        String texto = extrairTexto(resposta);
        if (texto.isBlank()) {
            String motivo = resposta.path("candidates").path(0).path("finishReason").asText("desconhecido");
            throw new IllegalStateException(
                    "Gemini nao retornou texto (finishReason=" + motivo + "). Resposta: " + resposta);
        }

        String limpo = texto.trim();
        if (limpo.startsWith("```")) {
            limpo = limpo.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        return objectMapper.readTree(limpo);
    }

    /**
     * Junta o texto de todas as partes da resposta, ignorando as partes de raciocinio
     * ("thought": true) que os modelos mais novos podem devolver antes do conteudo.
     */
    private static String extrairTexto(JsonNode resposta) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode parte : resposta.path("candidates").path(0).path("content").path("parts")) {
            if (parte.path("thought").asBoolean(false)) {
                continue;
            }
            String t = parte.path("text").asText("");
            if (!t.isBlank()) {
                sb.append(t);
            }
        }
        return sb.toString();
    }
}
