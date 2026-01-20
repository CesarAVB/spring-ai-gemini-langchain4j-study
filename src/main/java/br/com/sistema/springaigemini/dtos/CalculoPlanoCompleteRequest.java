package br.com.sistema.springaigemini.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que agrega os dados necessários para calcular um plano nutricional.
 * 
 * Este é o request final que o serviço de assistentes recebe.
 * Inclui dados do paciente e avaliação física (vindos de outro microserviço).
 */
public record CalculoPlanoCompleteRequest(
        @NotNull(message = "Dados do paciente são obrigatórios")
        PacienteDTO paciente,

        @NotNull(message = "Dados da avaliação física são obrigatórios")
        AvaliacaoFisicaDTO avaliacaoFisica,

        @NotBlank(message = "Objetivo é obrigatório")
        String objetivo,

        @NotBlank(message = "Intensidade de exercício é obrigatória")
        String intensidadeExercicio,

        String observacoes
) { }