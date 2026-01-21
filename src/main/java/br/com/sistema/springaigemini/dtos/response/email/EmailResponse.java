package br.com.sistema.springaigemini.dtos.response.email;

import java.util.List;

/**
 * Response para dados de emails
 */
public record EmailResponse(
    Integer total,
    List<EmailInfoResponse> emails
) {}