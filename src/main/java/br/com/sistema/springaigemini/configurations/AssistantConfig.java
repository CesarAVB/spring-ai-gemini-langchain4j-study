package br.com.sistema.springaigemini.configurations;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.log4j.Log4j2;

/**
 * ==================== CONFIGURAÇÃO CENTRAL DO PROJETO ====================
 * 
 * Esta classe é responsável por registrar TODOS os beans do projeto.
 * 
 * Beans registrados:
 * 1. GoogleAiGeminiChatModel - Modelo de IA Gemini
 * 2. Gmail API Service - Gerenciamento de emails Gmail
 * 3. RestTemplate - Cliente HTTP
 * 4. ObjectMapper - Serialização JSON
 * 
 * MAPPERS (registrados como beans via @Component ou @Mapper):
 * ===========================================================
 * Os mappers são registrados automaticamente pelo MapperStruct:
 * - CreatePlanoRequestMapper (request/plano/)
 * - PlanoResponseMapper (response/plano/)
 * - EmailResponseMapper (response/email/)
 * - RepositorioResponseMapper (response/repositorio/)
 * - RespostaAssistenteMapper (response/common/)
 * - ChatMessageRequestMapper (request/common/)
 * 
 * TOOLS REGISTRADAS (via @Component):
 * =====================================
 * - GithubAssistantTools - Gerenciamento de repositórios GitHub
 *   (Detectada automaticamente via @Component)
 * 
 * - GmailAssistantTools - Gerenciamento de emails Gmail
 *   (Detectada automaticamente via @Component)
 * 
 * As tools são injetadas automaticamente nos serviços que as necessitam.
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
    
    // ==================== 2. GMAIL API ====================
    
    /**
     * Cria e configura o cliente da Gmail API.
     * 
     * Usa OAuth2 com credenciais configuradas em application.properties:
     * - gmail.client-id
     * - gmail.client-secret
     * - gmail.redirect-uri
     * 
     * Na primeira execução, abre o navegador para autorização.
     * Os tokens são salvos em pasta "tokens/" para uso futuro.
     * 
     * @return cliente Gmail configurado e autenticado
     * @throws GeneralSecurityException se houver erro de segurança
     * @throws IOException se houver erro ao ler credenciais
     */
    @Bean
    public Gmail gmailService(
            @Value("${gmail.client-id}") String clientId,
            @Value("${gmail.client-secret}") String clientSecret,
            @Value("${gmail.redirect-uri:http://localhost:8888/Callback}") String redirectUri
    ) throws GeneralSecurityException, IOException {
        
        log.info("========================================");
        log.info("Inicializando Gmail API Service");
        log.info("========================================");
        
        try {
            File tokensDirectory = new File("tokens");
            
            log.info("Criando Google Client Secrets...");
            
            // Criar secrets a partir do application.properties
            com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets clientSecrets = 
                new com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets()
                    .setInstalled(new com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUris(Collections.singletonList(redirectUri)));
            
            // Criar o flow de autorização
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    Collections.singletonList(GmailScopes.GMAIL_MODIFY)
            )
            .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory))
            .setAccessType("offline")
            .build();
            
            log.info("Flow de autorização criado com sucesso");
            
            // Criar app instalada (desktop)
            com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp app = 
                new com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp(
                    flow, 
                    new com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver.Builder()
                        .setPort(8888)
                        .build()
                );
            
            // Obter credencial
            log.info("Obtendo credenciais do usuário...");
            com.google.api.client.auth.oauth2.Credential credential = app.authorize("user");
            
            log.info("✅ Credenciais obtidas com sucesso");
            
            // Criar serviço Gmail
            Gmail gmailService = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            )
            .setApplicationName("Spring AI Gmail Assistant")
            .build();
            
            log.info("========================================");
            log.info("✅ Gmail API Service inicializado com sucesso");
            log.info("========================================");
            
            return gmailService;
            
        } catch (IOException e) {
            log.error("❌ Erro ao inicializar Gmail API", e);
            throw e;
        } catch (GeneralSecurityException e) {
            log.error("❌ Erro de segurança ao inicializar Gmail API", e);
            throw e;
        }
    }
    
    // ==================== 3. REST TEMPLATE E OBJECT MAPPER ====================
    
    /**
     * Bean RestTemplate para requisições HTTP.
     * Usado por serviços que precisam fazer chamadas HTTP.
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("Criando RestTemplate");
        return new RestTemplate();
    }
    
    /**
     * Bean ObjectMapper para serialização/desserialização JSON.
     * Usado para converter objetos Java para JSON e vice-versa.
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.info("Criando ObjectMapper");
        return new ObjectMapper();
    }
    
    // ==================== 4. MAPPERS ====================
    
    /**
     * MAPPERS (Conversão de DTOs)
     * 
     * Os mappers são registrados automaticamente pelo MapperStruct quando usam:
     * @Mapper(componentModel = "spring")
     * 
     * Mappers disponíveis:
     * 
     * REQUEST MAPPERS:
     * ----------------
     * - CreatePlanoRequestMapper (dtos/request/plano/)
     *   Converte CreatePlanoRequest → PlanoNutricional
     * 
     * - ChatMessageRequestMapper (dtos/request/common/)
     *   Valida ChatMessageRequest
     * 
     * RESPONSE MAPPERS:
     * -----------------
     * - PlanoResponseMapper (mappers/response/plano/)
     *   Converte PlanoNutricional → PlanoResponse
     * 
     * - EmailResponseMapper (mappers/response/email/)
     *   Converte Email → EmailResponse
     * 
     * - RepositorioResponseMapper (mappers/response/repositorio/)
     *   Converte Repositorio → RepositorioResponse
     * 
     * - RespostaAssistenteMapper (mappers/response/common/)
     *   Wrappa qualquer resposta em RespostaAssistente
     * 
     * Como injetar em um serviço:
     * ============================
     * @Service
     * public class MinhaService {
     *     @Autowired
     *     private PlanoResponseMapper planoMapper;
     *     
     *     public PlanoResponse mapear(PlanoNutricional plano) {
     *         return planoMapper.toPlanoResponse(plano);
     *     }
     * }
     */
    
    // ==================== 5. TOOLS (REGISTRADAS VIA @Component) ====================
    
    /**
     * GithubAssistantTools
     * 
     * Detectada automaticamente via anotação @Component.
     * Fornece operações para gerenciar repositórios GitHub:
     * 
     * - Listar repositórios
     * - Obter informações do repositório
     * - Listar/ler/criar/atualizar/deletar arquivos
     * - Gerar README.md automaticamente
     * - Listar issues abertas
     * - Listar pull requests
     * - Listar commits
     * - Listar branches
     * - Obter estatísticas de linguagem
     * 
     * Configuração necessária em application.properties:
     * github.token=${GITHUB_TOKEN}
     * github.username=${GITHUB_USERNAME}
     */
    
    /**
     * GmailAssistantTools
     * 
     * Detectada automaticamente via anotação @Component.
     * Fornece operações para gerenciar emails Gmail:
     * 
     * - Listar emails
     * - Enviar emails
     * - Deletar emails (permanentemente)
     * - Mover para lixo
     * - Esvaziar lixo
     * - Marcar como lido/não lido
     * - Listar não lidos
     * - Buscar emails
     * - Listar de remetente
     * - Deletar de remetente
     * - Contar emails
     * 
     * Usa Gmail API (configurada acima como bean)
     */
}