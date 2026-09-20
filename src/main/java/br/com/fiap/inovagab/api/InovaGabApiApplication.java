package br.com.fiap.inovagab.api;

import br.com.fiap.inovagab.api.config.IaProperties;
import br.com.fiap.inovagab.api.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, IaProperties.class})
public class InovaGabApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InovaGabApiApplication.class, args);
    }
}
