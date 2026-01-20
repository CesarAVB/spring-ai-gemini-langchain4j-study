package br.com.sistema.springaigemini.tools;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

import org.springframework.stereotype.Component;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import br.com.sistema.springaigemini.core.AssistantTool;
import dev.langchain4j.agent.tool.Tool;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools (ferramentas) para o assistente Gmail.
 * 
 * Operações disponíveis:
 * - Listar emails da caixa de entrada
 * - Enviar novo email
 * - Deletar email
 * - Marcar email como lido
 * - Obter conteúdo completo de um email
 * 
 * Implementa AssistantTool para descoberta automática.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GmailAssistantTools implements AssistantTool {

    private final Gmail gmailService;

    @Override
    public String getToolName() {
        return "GmailTools";
    }

    @Override
    public String getToolDescription() {
        return "Ferramentas para gerenciar emails no Gmail";
    }

    /**
     * Lista emails da caixa de entrada.
     * 
     * @param maxResults número máximo de emails a retornar (ex: 10)
     * @return lista formatada de emails com de, assunto, data e ID
     */
    @Tool("Lista os emails da caixa de entrada do Gmail")
    public String listEmails(int maxResults) {
        try {
            log.info("Listando últimos {} emails", maxResults);
            
            var result = gmailService.users()
                    .messages()
                    .list("me")
                    .setMaxResults((long) maxResults)
                    .execute();

            if (result.getMessages() == null || result.getMessages().isEmpty()) {
                log.warn("Nenhum email encontrado");
                return "📭 Nenhum email encontrado na caixa de entrada.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📧 **EMAILS DA CAIXA DE ENTRADA**\n");
            sb.append(String.format("Total de emails retornados: %d\n\n", result.getMessages().size()));
            
            for (int i = 0; i < result.getMessages().size(); i++) {
                Message message = result.getMessages().get(i);
                
                var full = gmailService.users()
                        .messages()
                        .get("me", message.getId())
                        .execute();

                String subject = getHeaderValue(full, "Subject");
                String from = getHeaderValue(full, "From");
                String date = getHeaderValue(full, "Date");
                String messageId = message.getId();

                sb.append(String.format(
                        "%d. 📌 **De:** %s\n" +
                        "   **Assunto:** %s\n" +
                        "   **Data:** %s\n" +
                        "   **ID:** %s\n\n",
                        i + 1, from, subject, date, messageId
                ));
            }

            log.info("✅ Listagem de emails realizada com sucesso");
            return sb.toString();

        } catch (Exception e) {
            log.error("Erro ao listar emails", e);
            return "❌ Erro ao listar emails: " + e.getMessage();
        }
    }

    /**
     * Envia um novo email.
     * 
     * @param to email destinatário (ex: joao@email.com)
     * @param subject assunto do email
     * @param body corpo/conteúdo do email
     * @return confirmação de envio
     */
    @Tool("Envia um novo email para um destinatário específico")
    public String sendEmail(String to, String subject, String body) {
        try {
            log.info("Enviando email para: {}", to);
            
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);

            email.setFrom("me");
            email.addRecipients(RecipientType.TO, to);
            email.setSubject(subject);
            email.setText(body);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);

            byte[] emailBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().encodeToString(emailBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);

            Message sentMessage = gmailService.users()
                    .messages()
                    .send("me", message)
                    .execute();

            log.info("✅ Email enviado com sucesso para: {} com ID: {}", to, sentMessage.getId());
            
            return String.format(
                    "✅ **EMAIL ENVIADO COM SUCESSO**\n\n" +
                    "**Para:** %s\n" +
                    "**Assunto:** %s\n" +
                    "**ID da Mensagem:** %s\n\n" +
                    "Conteúdo:\n%s",
                    to, subject, sentMessage.getId(), body
            );

        } catch (Exception e) {
            log.error("Erro ao enviar email para: {}", to, e);
            return "❌ Erro ao enviar email: " + e.getMessage();
        }
    }

    /**
     * Deleta um email específico.
     * 
     * CUIDADO: Esta operação é permanente!
     * 
     * @param messageId ID do email a deletar
     * @return confirmação de deleção
     */
    @Tool("Deleta um email do Gmail baseado no ID")
    public String deleteEmail(String messageId) {
        try {
            log.info("Deletando email com ID: {}", messageId);
            
            gmailService.users()
                    .messages()
                    .delete("me", messageId)
                    .execute();

            log.info("✅ Email deletado com sucesso: {}", messageId);
            return String.format(
                    "✅ **EMAIL DELETADO COM SUCESSO**\n\n" +
                    "ID do email deletado: %s\n" +
                    "Status: Movido para lixo",
                    messageId
            );

        } catch (Exception e) {
            log.error("Erro ao deletar email com ID: {}", messageId, e);
            return "❌ Erro ao deletar email: " + e.getMessage();
        }
    }

    /**
     * Marca um email como lido.
     * 
     * Remove a label UNREAD do email.
     * 
     * @param messageId ID do email
     * @return confirmação da ação
     */
    @Tool("Marca um email como lido no Gmail")
    public String markAsRead(String messageId) {
        try {
            log.info("Marcando email como lido: {}", messageId);
            
            com.google.api.services.gmail.model.ModifyMessageRequest mods =
                    new com.google.api.services.gmail.model.ModifyMessageRequest()
                            .setRemoveLabelIds(java.util.List.of("UNREAD"));

            gmailService.users()
                    .messages()
                    .modify("me", messageId, mods)
                    .execute();

            log.info("✅ Email marcado como lido: {}", messageId);
            return String.format(
                    "✅ **EMAIL MARCADO COMO LIDO**\n\n" +
                    "ID do email: %s\n" +
                    "Status: Removido de não lidos",
                    messageId
            );

        } catch (Exception e) {
            log.error("Erro ao marcar email como lido: {}", messageId, e);
            return "❌ Erro ao marcar como lido: " + e.getMessage();
        }
    }

    /**
     * Obtém o conteúdo completo de um email.
     * 
     * Inclui: De, Assunto, Data e Corpo da mensagem
     * 
     * @param messageId ID do email
     * @return conteúdo completo formatado
     */
    @Tool("Obtém e exibe o conteúdo completo de um email específico")
    public String getEmailContent(String messageId) {
        try {
            log.info("Obtendo conteúdo do email: {}", messageId);
            
            var message = gmailService.users()
                    .messages()
                    .get("me", messageId)
                    .setFormat("full")
                    .execute();

            String subject = getHeaderValue(message, "Subject");
            String from = getHeaderValue(message, "From");
            String date = getHeaderValue(message, "Date");
            String body = getBodyContent(message);

            log.info("✅ Conteúdo obtido com sucesso para: {}", messageId);

            return String.format(
                    "📧 **CONTEÚDO COMPLETO DO EMAIL**\n\n" +
                    "**De:** %s\n" +
                    "**Assunto:** %s\n" +
                    "**Data:** %s\n" +
                    "**ID:** %s\n\n" +
                    "**Corpo da Mensagem:**\n%s",
                    from, subject, date, messageId, body
            );

        } catch (Exception e) {
            log.error("Erro ao obter conteúdo do email: {}", messageId, e);
            return "❌ Erro ao obter conteúdo: " + e.getMessage();
        }
    }

    /**
     * Marca um email como não lido.
     * 
     * Adiciona a label UNREAD ao email.
     * 
     * @param messageId ID do email
     * @return confirmação da ação
     */
    @Tool("Marca um email como não lido no Gmail")
    public String markAsUnread(String messageId) {
        try {
            log.info("Marcando email como não lido: {}", messageId);
            
            com.google.api.services.gmail.model.ModifyMessageRequest mods =
                    new com.google.api.services.gmail.model.ModifyMessageRequest()
                            .setAddLabelIds(java.util.List.of("UNREAD"));

            gmailService.users()
                    .messages()
                    .modify("me", messageId, mods)
                    .execute();

            log.info("✅ Email marcado como não lido: {}", messageId);
            return String.format(
                    "✅ **EMAIL MARCADO COMO NÃO LIDO**\n\n" +
                    "ID do email: %s\n" +
                    "Status: Adicionado a não lidos",
                    messageId
            );

        } catch (Exception e) {
            log.error("Erro ao marcar email como não lido: {}", messageId, e);
            return "❌ Erro ao marcar como não lido: " + e.getMessage();
        }
    }

    /**
     * Lista emails não lidos.
     * 
     * @param maxResults número máximo de emails a retornar
     * @return lista de emails não lidos
     */
    @Tool("Lista todos os emails não lidos da caixa de entrada")
    public String listUnreadEmails(int maxResults) {
        try {
            log.info("Listando últimos {} emails não lidos", maxResults);
            
            var result = gmailService.users()
                    .messages()
                    .list("me")
                    .setQ("is:unread")
                    .setMaxResults((long) maxResults)
                    .execute();

            if (result.getMessages() == null || result.getMessages().isEmpty()) {
                log.info("Nenhum email não lido encontrado");
                return "✅ Parabéns! Você não tem emails não lidos.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📧 **EMAILS NÃO LIDOS**\n");
            sb.append(String.format("Total: %d\n\n", result.getMessages().size()));
            
            for (int i = 0; i < result.getMessages().size(); i++) {
                Message message = result.getMessages().get(i);
                
                var full = gmailService.users()
                        .messages()
                        .get("me", message.getId())
                        .execute();

                String subject = getHeaderValue(full, "Subject");
                String from = getHeaderValue(full, "From");
                String date = getHeaderValue(full, "Date");
                String messageId = message.getId();

                sb.append(String.format(
                        "%d. 🔴 **De:** %s\n" +
                        "   **Assunto:** %s\n" +
                        "   **Data:** %s\n" +
                        "   **ID:** %s\n\n",
                        i + 1, from, subject, date, messageId
                ));
            }

            log.info("✅ Listagem de emails não lidos realizada com sucesso");
            return sb.toString();

        } catch (Exception e) {
            log.error("Erro ao listar emails não lidos", e);
            return "❌ Erro ao listar emails não lidos: " + e.getMessage();
        }
    }

    /**
     * Busca emails por palavra-chave.
     * 
     * @param query palavra-chave a buscar
     * @param maxResults número máximo de resultados
     * @return emails encontrados
     */
    @Tool("Busca emails que contenham uma palavra-chave específica")
    public String searchEmails(String query, int maxResults) {
        try {
            log.info("Buscando emails com query: {}", query);
            
            var result = gmailService.users()
                    .messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults((long) maxResults)
                    .execute();

            if (result.getMessages() == null || result.getMessages().isEmpty()) {
                log.info("Nenhum email encontrado para a busca: {}", query);
                return String.format("Nenhum email encontrado para: '%s'", query);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔍 **RESULTADOS DA BUSCA: '%s'**\n", query));
            sb.append(String.format("Total encontrado: %d\n\n", result.getMessages().size()));
            
            for (int i = 0; i < result.getMessages().size(); i++) {
                Message message = result.getMessages().get(i);
                
                var full = gmailService.users()
                        .messages()
                        .get("me", message.getId())
                        .execute();

                String subject = getHeaderValue(full, "Subject");
                String from = getHeaderValue(full, "From");
                String date = getHeaderValue(full, "Date");
                String messageId = message.getId();

                sb.append(String.format(
                        "%d. 📌 **De:** %s\n" +
                        "   **Assunto:** %s\n" +
                        "   **Data:** %s\n" +
                        "   **ID:** %s\n\n",
                        i + 1, from, subject, date, messageId
                ));
            }

            log.info("✅ Busca realizada com sucesso. {} resultados encontrados", 
                    result.getMessages().size());
            return sb.toString();

        } catch (Exception e) {
            log.error("Erro ao buscar emails com query: {}", query, e);
            return "❌ Erro na busca: " + e.getMessage();
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Extrai um valor de header específico da mensagem.
     * 
     * @param message mensagem do Gmail
     * @param headerName nome do header (ex: "Subject", "From", "Date")
     * @return valor do header ou "N/A" se não encontrado
     */
    private String getHeaderValue(Message message, String headerName) {
        if (message.getPayload() == null || 
            message.getPayload().getHeaders() == null) {
            return "N/A";
        }

        return message.getPayload()
                .getHeaders()
                .stream()
                .filter(h -> headerName.equals(h.getName()))
                .map(com.google.api.services.gmail.model.MessagePartHeader::getValue)
                .findFirst()
                .orElse("N/A");
    }

    /**
     * Extrai o corpo/conteúdo da mensagem.
     * 
     * Tenta decodificar o conteúdo em base64.
     * Se falhar, retorna mensagem de erro.
     * 
     * @param message mensagem do Gmail
     * @return conteúdo decodificado ou mensagem de erro
     */
    private String getBodyContent(Message message) {
        try {
            MessagePart part = message.getPayload();
            
            // Tentar obter do corpo principal
            if (part.getBody() != null && part.getBody().getData() != null) {
                String data = part.getBody().getData();
                byte[] decode = Base64.getUrlDecoder().decode(data);
                return new String(decode, java.nio.charset.StandardCharsets.UTF_8);
            }

            // Tentar obter das partes (multipart)
            if (part.getParts() != null) {
                for (MessagePart p : part.getParts()) {
                    if (p.getBody() != null && p.getBody().getData() != null) {
                        String data = p.getBody().getData();
                        byte[] decode = Base64.getUrlDecoder().decode(data);
                        return new String(decode, java.nio.charset.StandardCharsets.UTF_8);
                    }
                }
            }

            return "Conteúdo não disponível ou é um email com anexos";
        } catch (Exception e) {
            log.warn("Erro ao decodificar corpo do email", e);
            return "Erro ao decodificar conteúdo: " + e.getMessage();
        }
    }
}