package br.com.fiap.inovagab.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    OPERADOR("operador"),
    GESTOR("gestor"),
    LIDER("lider");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    @JsonCreator
    public static Role from(String texto) {
        for (Role r : values()) {
            if (r.value.equalsIgnoreCase(texto) || r.name().equalsIgnoreCase(texto)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Perfil invalido: " + texto);
    }
}
