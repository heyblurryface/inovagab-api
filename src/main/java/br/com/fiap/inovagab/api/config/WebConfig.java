package br.com.fiap.inovagab.api.config;

import br.com.fiap.inovagab.api.domain.EtapaProjeto;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite usar os valores minusculos dos enums (ex.: ?status=em_andamento) em query params. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, StatusIdeia>() {
            @Override
            public StatusIdeia convert(String source) {
                return StatusIdeia.from(source);
            }
        });
        registry.addConverter(new Converter<String, StatusProjeto>() {
            @Override
            public StatusProjeto convert(String source) {
                return StatusProjeto.from(source);
            }
        });
        registry.addConverter(new Converter<String, EtapaProjeto>() {
            @Override
            public EtapaProjeto convert(String source) {
                return EtapaProjeto.from(source);
            }
        });
    }
}
