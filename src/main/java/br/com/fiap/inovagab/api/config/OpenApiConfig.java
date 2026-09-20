package br.com.fiap.inovagab.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inovaGabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("InovaGAB API")
                        .version("1.0.0")
                        .description("""
                                Backend da plataforma de inovacao InovaGAB (Challenge Grupo Aguia Branca / FIAP).
                                Autentique em POST /api/auth/login e use o token no botao "Authorize" (Bearer).
                                Perfis: operador, gestor e lider."""))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
