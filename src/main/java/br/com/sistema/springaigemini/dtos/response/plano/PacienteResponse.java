package br.com.sistema.springaigemini.dtos.response.plano;

/**
 * Response para informações do paciente
 */
public record PacienteResponse(
    Integer id,
    String nome,
    String email,
    String sexo,
    Double altura,
    String dataNascimento
) {}