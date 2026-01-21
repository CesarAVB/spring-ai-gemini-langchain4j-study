package br.com.sistema.springaigemini.dtos.request.common;

/**
 * Request para enviar mensagem para assistente
 */
public record ChatMessageRequest(
    String message
) {}