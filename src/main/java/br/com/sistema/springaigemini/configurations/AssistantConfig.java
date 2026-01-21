package br.com.sistema.springaigemini.configurations;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.Gmail;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class AssistantConfig {
    
    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${ai.gemini.model}")
    private String geminiModel;

    @Autowired
    private GmailAuthSetup gmailAuthSetup; // Injetando a classe que lida com o Refresh Token
    
    // ==================== 1. MODELO DE IA ====================
    
    @Bean
    public GoogleAiGeminiChatModel googleAiGeminiChatModel() {
        log.info("Inicializando GoogleAiGeminiChatModel: {}", geminiModel);
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModel)
                .build();
    }
    
    // ==================== 2. GMAIL API (CORRIGIDO) ====================
    
    @Bean
    public Gmail gmailService() throws GeneralSecurityException, IOException {
        log.info("========================================");
        log.info("Iniciando Gmail API via GmailAuthSetup");
        log.info("========================================");
        
        try {
            // Chamamos o método que criamos na outra classe que não abre navegador
            return gmailAuthSetup.getGmailService();
        } catch (Exception e) {
            log.error("❌ Falha fatal ao obter serviço Gmail: {}", e.getMessage());
            throw new IOException(e);
        }
    }
    
    // ==================== 3. REST TEMPLATE E OBJECT MAPPER ====================
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}