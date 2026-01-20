package br.com.sistema.springaigemini.core;

import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

/**
 * Service abstrato que fornece funcionalidades comuns para todos os assistentes.
 * 
 * Uso:
 * 1. Crie uma classe que estenda esta (ex: AssistanteNutricionalService)
 * 2. Defina o @SystemMessage
 * 3. Injete as tools específicas
 * 4. Implemente processMessage() chamando o AiService
 */
@Service
@Log4j2
public abstract class BaseAssistantService implements GenericAssistant {

    /**
     * Método a ser implementado pelos serviços específicos.
     * Aqui é onde você injeta o AiService com suas tools específicas.
     */
    @Override
    public abstract String processMessage(String userMessage);

    /**
     * Método auxiliar para logging de interações.
     */
    protected void logInteraction(String assistantName, String userMessage, String response) {
        log.info("Assistente: {} | Entrada: {} | Saída (primeiras 100 chars): {}",
                assistantName,
                userMessage.substring(0, Math.min(50, userMessage.length())),
                response.substring(0, Math.min(100, response.length()))
        );
    }

    /**
     * Método auxiliar para tratamento de erros.
     */
    protected String handleError(Exception e, String assistantName) {
        log.error("Erro no assistente {}: {}", assistantName, e.getMessage(), e);
        return String.format("❌ Erro ao processar mensagem: %s", e.getMessage());
    }
}