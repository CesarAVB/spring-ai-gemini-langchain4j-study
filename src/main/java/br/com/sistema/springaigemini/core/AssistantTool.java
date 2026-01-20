package br.com.sistema.springaigemini.core;

/**
 * Interface base para ferramentas (tools) que serão utilizadas pelos assistentes.
 * As implementações devem ser anotadas com @Component para serem descobertas automaticamente.
 * 
 * Padrão: Seus métodos devem ser anotados com @Tool do LangChain4j.
 */
public interface AssistantTool {

    /**
     * Retorna o nome da ferramenta.
     * Ex: "CalculadorPlanoNutricional", "CalculadorQuotacaoLocadora"
     */
    String getToolName();

    /**
     * Retorna descrição breve do que a ferramenta faz.
     */
    String getToolDescription();
}