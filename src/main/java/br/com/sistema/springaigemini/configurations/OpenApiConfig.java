package br.com.sistema.springaigemini.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do Swagger/OpenAPI
 * 
 * Acesse em: http://localhost:8080/swagger-ui.html
 * JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Gemini - Assistentes IA")
                        .version("1.0.0")
                        .description("""
                        		API REST para gerenciar Assistentes de IA integrados com Google Gemini,
                                Gmail e GitHub.
                                
                                **Recursos Disponíveis:**
                                - 📧 **Gmail Assistant**: Gerenciar emails (listar, enviar, deletar, buscar)
                                - 📚 **GitHub Assistant**: Gerenciar repositórios (listar, criar/editar arquivos, issues)
                                - 📋 **Plano Nutricional**: Cálculos e recomendações personalizadas
                                
                                **Tecnologias:**
                                - Spring Boot 3.2.5
                                - Java 21
                                - LangChain4j
                                - Google Gemini AI
                                - Gmail API
                                - GitHub API
                                - MapperStruct
                        		""")
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
