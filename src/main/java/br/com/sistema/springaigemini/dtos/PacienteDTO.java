package br.com.sistema.springaigemini.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * DTO para receber dados do Paciente.
 * 
 * Este DTO é usado para comunicação com o microserviço de pacientes.
 * Não depende de entidades JPA do projeto de nutrição.
 */
public record PacienteDTO(
        Long id,

        String nome,

        String sexo,

        Double altura,

        @JsonProperty("data_nascimento")
        LocalDate dataNascimento,

        String cpf,

        String email,

        String telefone
) { }