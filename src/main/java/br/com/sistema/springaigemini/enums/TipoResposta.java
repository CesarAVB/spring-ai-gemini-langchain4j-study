package br.com.sistema.springaigemini.enums;

/**
 * Enum para tipos de resposta de assistentes
 * 
 * Define os tipos possíveis de resposta que um assistente pode retornar
 */
public enum TipoResposta {
    TEXTO("texto", "Resposta em texto simples"),
    ESTRUTURADO("estruturado", "Resposta estruturada/dados"),
    ERRO("erro", "Resposta de erro");
    
    private final String valor;
    private final String descricao;
    
    TipoResposta(String valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }
    
    public String getValor() {
        return valor;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Obtém enum pelo valor
     */
    public static TipoResposta fromValor(String valor) {
        for (TipoResposta tipo : TipoResposta.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de resposta inválido: " + valor);
    }
}