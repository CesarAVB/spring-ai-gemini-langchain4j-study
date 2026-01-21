package br.com.sistema.springaigemini.dtos.response.email;

/**
 * Response para remetente do email
 */
public record RemetenteResponse(
    String nome,
    String email
) {}