package br.com.fiap.inovagab.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracao da IA.
 *
 * <p>{@code geminiModel} aceita uma lista separada por virgula. A cota gratuita da Gemini e
 * contada <b>por modelo</b>, entao declarar mais de um da folego: se o primeiro esgotar a cota
 * do dia, a API tenta o proximo antes de cair no avaliador heuristico local.</p>
 */
@ConfigurationProperties(prefix = "inovagab.ia")
public record IaProperties(String geminiApiKey, String geminiModel, boolean fallbackHeuristica) {

    public boolean geminiConfigurado() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }

    /** Modelos na ordem de preferencia. */
    public List<String> geminiModelos() {
        if (geminiModel == null || geminiModel.isBlank()) {
            return List.of("gemini-3.6-flash");
        }
        return Arrays.stream(geminiModel.split(","))
                .map(String::trim)
                .filter(m -> !m.isBlank())
                .toList();
    }
}
