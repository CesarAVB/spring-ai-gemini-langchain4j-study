package br.com.sistema.springaigemini.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Gemini LangChain4j Study API") // Título atualizado
                        .version("1.0.0")
                        .description("API de estudo e exploração da integração de IA com Google Gemini e LangChain4j em Spring Boot. " +
                                     "Demonstra funcionalidades de LLMs, como chatbots, processamento de linguagem natural e geração de conteúdo.") // Descrição atualizada
                        .contact(new Contact()
                                .name("César Augusto")
                                .email("cesar.augusto.rj1@gmail.com")
                                .url("https://portfolio.cesaravb.com.br/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento Local"),
                        new Server()
                                .url("https://api-langchain4j.cesaravb.com.br/")
                                .description("Servidor de Produção")
                ));
    }
}
