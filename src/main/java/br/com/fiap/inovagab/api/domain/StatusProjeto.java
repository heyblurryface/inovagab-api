package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusProjeto {
    EM_ANDAMENTO("em_andamento"),
    PAUSADO("pausado"),
    CONCLUIDO("concluido"),
    CANCELADO("cancelado");

    private final String value;

    StatusProjeto(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusProjeto from(String texto) {
        for (StatusProjeto s : values()) {
            if (s.value.equalsIgnoreCase(texto) || s.name().equalsIgnoreCase(texto)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status de projeto invalido: " + texto);
    }
}
