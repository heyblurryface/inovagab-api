package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusIdeia {
    PENDENTE("pendente"),
    APROVADA("aprovada"),
    REJEITADA("rejeitada");

    private final String value;

    StatusIdeia(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusIdeia from(String texto) {
        for (StatusIdeia s : values()) {
            if (s.value.equalsIgnoreCase(texto) || s.name().equalsIgnoreCase(texto)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status de ideia invalido: " + texto);
    }
}
