package br.com.sistema.springaigemini.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.sistema.springaigemini.mappers.CalculoPlanoMapper;
import br.com.sistema.springaigemini.mappers.MacronutrientesMapper;
import br.com.sistema.springaigemini.mappers.PlanoNutricionalMapper;
import br.com.sistema.springaigemini.services.PlanoNutricionalCalculatorService;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.log4j.Log4j2;

/**
 * ==================== CONFIGURAÇÃO CENTRAL DO PROJETO ====================
 * 
 * Esta classe é responsável por registrar TODOS os beans do projeto.
 * 
 * Beans registrados:
 * 1. GoogleAiGeminiChatModel - Modelo de IA Gemini
 * 2. PlanoNutricionalCalculatorService - Serviço de cálculo
 * 3. AssistantPlanoService - Serviço de assistente
 * 4. Mappers - Conversão de DTOs
 */
@Log4j2
@Configuration
public class AssistantConfig {
    
    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${ai.gemini.model}")
    private String geminiModel;
    
    // ==================== 1. MODELO DE IA ====================
    
    @Bean
    public GoogleAiGeminiChatModel googleAiGeminiChatModel() {
        log.info("========================================");
        log.info("Inicializando GoogleAiGeminiChatModel");
        log.info("========================================");
        log.info("Modelo: {}", geminiModel);
        
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModel)
                .build();
        
        log.info("✅ GoogleAiGeminiChatModel inicializado com sucesso");
        return model;
    }
    
    // ==================== 2. MAPPERS (Conversão de DTOs) ====================
    
    @Bean
    public PlanoNutricionalMapper planoNutricionalMapper() {
        log.info("Criando PlanoNutricionalMapper");
        return PlanoNutricionalMapper.INSTANCE;
    }
    
    @Bean
    public MacronutrientesMapper macronutrientesMapper() {
        log.info("Criando MacronutrientesMapper");
        return MacronutrientesMapper.INSTANCE;
    }
    
    @Bean
    public CalculoPlanoMapper calculoPlanoMapper() {
        log.info("Criando CalculoPlanoMapper");
        return CalculoPlanoMapper.INSTANCE;
    }
    
    // ==================== 3. SERVIÇOS DE CÁLCULO ====================
    
    @Bean
    public PlanoNutricionalCalculatorService planoNutricionalCalculatorService(
            PlanoNutricionalMapper planoNutricionalMapper) {
        
        log.info("========================================");
        log.info("Criando PlanoNutricionalCalculatorService");
        log.info("========================================");
        
        PlanoNutricionalCalculatorService service = 
                new PlanoNutricionalCalculatorService(planoNutricionalMapper);
        
        log.info("✅ PlanoNutricionalCalculatorService criado com sucesso");
        return service;
    }
}