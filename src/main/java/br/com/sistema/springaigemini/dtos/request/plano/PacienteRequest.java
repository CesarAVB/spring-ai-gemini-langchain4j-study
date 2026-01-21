package br.com.sistema.springaigemini.dtos.request.plano;

/**
 * Request para informações do paciente (entrada)
 */
public record PacienteRequest(
    String nome,
    String email,
    String sexo,
    Double altura,
    String dataNascimento,
    String cpf,
    String telefone
) {}