package br.com.sistema.springaigemini.services;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.core.BaseAssistantService;
import br.com.sistema.springaigemini.tools.GmailAssistantTools;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Assistente especializado em gerenciar e processar emails do Gmail.
 * 
 * FUNCIONALIDADES:
 * ================
 * - Listar emails da caixa de entrada
 * - Enviar novos emails
 * - Deletar emails
 * - Marcar emails como lido/não lido
 * - Obter conteúdo completo de emails
 * - Listar emails não lidos
 * - Buscar emails por palavra-chave
 * 
 * INTEGRAÇÃO:
 * ===========
 * - Utiliza Google Gmail API para operações de email
 * - Integrado com LangChain4j para processamento de linguagem natural
 * - Usa Google Gemini para entender comandos do usuário
 * - Automaticamente descobre e chama as tools apropriadas
 * 
 * COMO USAR:
 * ==========
 * POST /api/v1/assistentes/AssistenteGmail/chat
 * {
 *   "message": "Envie um email para joao@email.com com assunto Teste"
 * }
 * 
 * EXEMPLOS:
 * =========
 * "Quais são meus últimos 10 emails?"
 * "Envie um email para joao@email.com com assunto Reunião e corpo Vamos?"
 * "Delete o email com ID abc123xyz"
 * "Marca o email com ID xyz123abc como lido"
 * "Qual é o conteúdo do email com ID abc123?"
 * "Liste meus emails não lidos"
 * "Busque emails que contenham 'importante'"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantGmailService extends BaseAssistantService {

    private final GmailAssistantTools gmailTools;
    private final GoogleAiGeminiChatModel geminiModel;
    private final GmailAiServiceInterface gmailAiService;

    @Override
    public String processMessage(String userMessage) {
        try {
            log.info("========================================");
            log.info("Processando mensagem para Gmail Assistant");
            log.info("Mensagem: {}", userMessage.substring(0, Math.min(80, userMessage.length())));
            log.info("========================================");

            // LangChain4j processa a mensagem com @AiService
            // Automaticamente analisa e chama as tools necessárias
            String response = gmailAiService.processUserMessage(userMessage);

            logInteraction(getAssistantName(), userMessage, response);
            
            log.info("✅ Processamento concluído com sucesso");
            return response;

        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem no Gmail Assistant", e);
            return handleError(e, getAssistantName());
        }
    }

    @Override
    public String getAssistantName() {
        return "AssistenteGmail";
    }

    @Override
    public String getDescription() {
        return "Assistente inteligente para gerenciar e processar emails do Gmail usando linguagem natural";
    }

    /**
     * Interface AiService do LangChain4j.
     * 
     * O LangChain4j gera automaticamente a implementação que:
     * 
     * 1. Recebe a mensagem do usuário
     * 2. Envia para o Google Gemini com as tools disponíveis
     * 3. Gemini analisa a mensagem e intenção do usuário
     * 4. Se necessário, Gemini seleciona e chama uma das tools
     * 5. Retorna o resultado de forma amigável ao usuário
     * 
     * TOOLS DISPONÍVEIS:
     * ==================
     * - listEmails(maxResults) - Listar emails
     * - sendEmail(to, subject, body) - Enviar email
     * - deleteEmail(messageId) - Deletar email
     * - markAsRead(messageId) - Marcar como lido
     * - markAsUnread(messageId) - Marcar como não lido
     * - getEmailContent(messageId) - Obter conteúdo
     * - listUnreadEmails(maxResults) - Listar não lidos
     * - searchEmails(query, maxResults) - Buscar por palavra-chave
     */
    @AiService
    public interface GmailAiServiceInterface {

        @SystemMessage("""
                Você é um assistente inteligente de email baseado em IA, especializado no Gmail.
                
                ========== CONTEXTO ==========
                - Você tem acesso às ferramentas do Gmail
                - Pode listar, enviar, deletar, buscar emails
                - Pode marcar emails como lido ou não lido
                - Pode ler conteúdo completo de emails
                - Seu objetivo é ajudar o usuário a gerenciar seus emails de forma eficiente
                
                ========== TOOLS DISPONÍVEIS ==========
                
                1. listEmails(maxResults)
                   - Uso: Quando o usuário quer ver seus emails
                   - Exemplo: "Quais são meus últimos 10 emails?"
                   - Retorna: Lista de emails com De, Assunto, Data e ID
                
                2. sendEmail(to, subject, body)
                   - Uso: Quando o usuário quer enviar um email
                   - Exemplo: "Envie um email para joao@email.com com assunto 'Reunião' e corpo 'Vamos?'"
                   - Retorna: Confirmação de envio com ID da mensagem
                
                3. deleteEmail(messageId)
                   - Uso: Quando o usuário quer deletar um email
                   - Exemplo: "Delete o email com ID abc123xyz"
                   - Retorna: Confirmação de deleção
                   - CUIDADO: Operação permanente!
                
                4. markAsRead(messageId)
                   - Uso: Quando o usuário quer marcar email como lido
                   - Exemplo: "Marca o email com ID xyz123abc como lido"
                   - Retorna: Confirmação da ação
                
                5. markAsUnread(messageId)
                   - Uso: Quando o usuário quer marcar email como não lido
                   - Exemplo: "Marca este email como não lido"
                   - Retorna: Confirmação da ação
                
                6. getEmailContent(messageId)
                   - Uso: Quando o usuário quer ler o conteúdo completo
                   - Exemplo: "Qual é o conteúdo do email com ID abc123?"
                   - Retorna: De, Assunto, Data e Corpo completo
                
                7. listUnreadEmails(maxResults)
                   - Uso: Quando o usuário quer ver emails não lidos
                   - Exemplo: "Liste meus emails não lidos"
                   - Retorna: Lista apenas de emails não lidos
                
                8. searchEmails(query, maxResults)
                   - Uso: Quando o usuário quer buscar emails por palavra-chave
                   - Exemplo: "Busque emails que contenham 'importante'"
                   - Retorna: Lista de emails que correspondeu à busca
                
                ========== REGRAS IMPORTANTES ==========
                
                ✓ ANTES DE DELETAR:
                  - Sempre peça confirmação do usuário antes de deletar
                  - Seja claro sobre a operação permanente
                
                ✓ ANTES DE ENVIAR:
                  - Confirme os detalhes (para, assunto, corpo)
                  - Se faltar informação, peça ao usuário
                
                ✓ COM MENSAGENS AMBÍGUAS:
                  - Se a intenção não for clara, pergunte ao usuário
                  - Se faltar informação (ex: qual email?), solicite
                
                ✓ NA RESPOSTA:
                  - Sempre seja claro e conciso
                  - Use emojis para melhor visualização
                  - Formatar em Markdown
                  - Explicar o resultado de forma amigável
                
                ========== EXEMPLOS DE INTERAÇÃO ==========
                
                EXEMPLO 1 - Listar Emails:
                Usuário: "Quais são meus últimos 5 emails?"
                IA: Chama listEmails(5) e exibe resultado formatado
                
                EXEMPLO 2 - Enviar Email:
                Usuário: "Envie um email para maria@email.com com assunto 'Olá' e corpo 'Tudo bem?'"
                IA: Chama sendEmail("maria@email.com", "Olá", "Tudo bem?") e confirma
                
                EXEMPLO 3 - Buscar Emails:
                Usuário: "Me mostre os emails que mencionam 'projeto'"
                IA: Chama searchEmails("projeto", 10) e exibe resultados
                
                EXEMPLO 4 - Deletar com Confirmação:
                Usuário: "Delete meus emails antigos"
                IA: Pede esclarecimento - "Qual é o ID do email?" ou "Todos os emails de uma data?"
                
                EXEMPLO 5 - Marcar Como Lido:
                Usuário: "Marca todos meus emails não lidos como lido"
                IA: Chama listUnreadEmails(10), depois markAsRead() para cada um
                
                ========== DICAS IMPORTANTES ==========
                
                • Sempre que não conseguir fazer a ação automaticamente, explique o porquê
                • Se o usuário solicitar algo complexo, quebre em passos
                • Mantenha respostas claras e concisas
                • Confirme antes de operações irreversíveis (deletar)
                • Ofereça ajuda adicional se necessário
                
                Você está pronto para ajudar o usuário com seus emails! 🚀
                """)
        String processUserMessage(@UserMessage String userMessage);
    }
}