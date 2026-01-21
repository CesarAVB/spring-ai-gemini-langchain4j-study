package br.com.sistema.springaigemini.dtos.response.common;

import br.com.sistema.springaigemini.enums.TipoResposta;

/**
 * Response padrão para todos os assistentes
 */
public record RespostaAssistente(
    Boolean sucesso,
    String assistente,
    String tipo,
    String pergunta,
    Object dados,
    ErroInfo erro,
    String timestamp
) {
    
    public static RespostaAssistente sucesso(
            String assistente, 
            String tipo, 
            String pergunta, 
            Object dados) {
        return new RespostaAssistente(
            true,
            assistente,
            tipo,
            pergunta,
            dados,
            null,
            java.time.LocalDateTime.now().toString()
        );
    }
    
    public static RespostaAssistente sucesso(
            String assistente, 
            TipoResposta tipo, 
            String pergunta, 
            Object dados) {
        return sucesso(assistente, tipo.getValor(), pergunta, dados);
    }
    
    public static RespostaAssistente erro(
            String assistente, 
            String codigo, 
            String mensagem, 
            int statusHttp) {
        return new RespostaAssistente(
            false,
            assistente,
            TipoResposta.ERRO.getValor(),
            null,
            null,
            new ErroInfo(codigo, mensagem, statusHttp),
            java.time.LocalDateTime.now().toString()
        );
    }
    
    public static RespostaAssistente erro(String assistente, ErroInfo erro) {
        return new RespostaAssistente(
            false,
            assistente,
            TipoResposta.ERRO.getValor(),
            null,
            null,
            erro,
            java.time.LocalDateTime.now().toString()
        );
    }
}