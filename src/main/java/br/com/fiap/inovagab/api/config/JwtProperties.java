package br.com.fiap.inovagab.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inovagab.jwt")
public record JwtProperties(String secret, long expiracaoHoras) {
}
