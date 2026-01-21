package br.com.sistema.springaigemini.core;

import lombok.extern.log4j.Log4j2;

/**
 * Classe base para todos os assistentes
 * 
 * Fornece métodos comuns como logging e processamento de mensagens
 */
@Log4j2
public abstract class BaseAssistantService implements GenericAssistant {

    /**
     * Registra interação (pergunta e resposta) para auditoria
     * Overload 1: com 2 parâmetros (pergunta, resposta)
     * 
     * @param pergunta pergunta do usuário
     * @param resposta resposta do assistente
     */
    protected void logInteraction(String pergunta, String resposta) {
        try {
            // Validar se resposta é nula
            if (resposta == null) {
                log.warn("Resposta nula para pergunta: {}", pergunta);
                return;
            }
            
            Integer tamanhoResposta = resposta.length();
            log.info(
                "Interação registrada - Pergunta: {} caracteres | Resposta: {} caracteres",
                pergunta != null ? pergunta.length() : 0,
                tamanhoResposta
            );
        } catch (Exception e) {
            log.error("Erro ao registrar interação", e);
        }
    }

    /**
     * Registra interação com assistente (pergunta, resposta, assistente)
     * Overload 2: com 3 parâmetros (assistente, pergunta, resposta)
     * 
     * @param assistente nome do assistente
     * @param pergunta pergunta do usuário
     * @param resposta resposta do assistente
     */
    protected void logInteraction(String assistente, String pergunta, String resposta) {
        try {
            // Validar se resposta é nula
            if (resposta == null) {
                log.warn("[{}] Resposta nula para pergunta: {}", assistente, pergunta);
                return;
            }
            
            Integer tamanhoResposta = resposta.length();
            log.info(
                "[{}] Interação registrada - Pergunta: {} caracteres | Resposta: {} caracteres",
                assistente,
                pergunta != null ? pergunta.length() : 0,
                tamanhoResposta
            );
        } catch (Exception e) {
            log.error("Erro ao registrar interação", e);
        }
    }

    /**
     * Trata e registra erros do assistente
     * 
     * @param e exceção
     * @param contexto contexto do erro
     */
    protected void handleError(Exception e, String contexto) {
        try {
            log.error("Erro no assistente {}: {}", contexto, e.getMessage(), e);
        } catch (Exception ex) {
            log.error("Erro ao tratar erro", ex);
        }
    }

    /**
     * Log estruturado para debug
     */
    protected void logDebug(String mensagem, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(mensagem, args);
        }
    }

    /**
     * Log estruturado para erro
     */
    protected void logErro(String mensagem, Exception e) {
        log.error(mensagem, e);
    }

    /**
     * Log estruturado para info
     */
    protected void logInfo(String mensagem, Object... args) {
        log.info(mensagem, args);
    }
}