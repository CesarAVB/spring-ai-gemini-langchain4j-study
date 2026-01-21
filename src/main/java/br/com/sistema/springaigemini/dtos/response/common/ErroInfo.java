package br.com.sistema.springaigemini.dtos.response.common;

/**
 * Record para informações de erro
 * 
 * Encapsula detalhes de um erro em uma resposta de assistente
 */
public record ErroInfo(
    String codigo,
    String mensagem,
    Integer statusHttp
) {
    
    /**
     * Factory method para criar um ErroInfo
     */
    public static ErroInfo de(String codigo, String mensagem, Integer statusHttp) {
        return new ErroInfo(codigo, mensagem, statusHttp);
    }
    
    /**
     * Validação ao criar
     */
    public ErroInfo {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código de erro não pode ser nulo ou vazio");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem de erro não pode ser nula ou vazia");
        }
        if (statusHttp == null || statusHttp <= 0) {
            throw new IllegalArgumentException("Status HTTP deve ser válido (> 0)");
        }
    }
}