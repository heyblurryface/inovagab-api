package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EtapaProjeto {
    PLANEJAMENTO("planejamento"),
    EXECUCAO("execucao"),
    MONITORAMENTO("monitoramento"),
    CONCLUIDO("concluido");

    private final String value;

    EtapaProjeto(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EtapaProjeto from(String texto) {
        for (EtapaProjeto e : values()) {
            if (e.value.equalsIgnoreCase(texto) || e.name().equalsIgnoreCase(texto)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Etapa de projeto invalida: " + texto);
    }
}
