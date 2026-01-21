package br.com.sistema.springaigemini.dtos.response.email;

/**
 * Response para informações de cada email
 */
public record EmailInfoResponse(
    String id,
    Integer numero,
    RemetenteResponse remetente,
    String assunto,
    String data,
    String preview,
    Boolean naoLido,
    Boolean importante
) {}