package br.com.sistema.springaigemini.core;

	/**
 * Interface genérica para assistentes de IA.
 * Implementadores definem seu próprio contexto (@SystemMessage) e ferramentas.
 */
public interface GenericAssistant {

    /**
     * Processa uma mensagem do usuário e retorna resposta da IA.
     *
     * @param userMessage mensagem do usuário
     * @return resposta da IA baseada no contexto e ferramentas disponíveis
     */
    String processMessage(String userMessage);

    /**
     * Retorna o nome/tipo do assistente.
     * Ex: "AssistenteNutricional", "AssistentePacientes", "AssistenteLocadora"
     */
    String getAssistantName();

    /**
     * Retorna a descrição breve do assistente.
     */
    String getDescription();
}