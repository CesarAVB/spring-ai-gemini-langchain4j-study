package br.com.sistema.springaigemini.dtos;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlanoNutricionalDTO(
        Long pacienteId,

        String nomePaciente,

        @JsonProperty("altura_metros")
        Double alturaMetros,

        @JsonProperty("peso_atual")
        Double pesoAtual,

        Integer idade,

        String objetivo,

        @JsonProperty("intensidade_exercicio")
        String intensidadeExercicio,

        @JsonProperty("tmb")
        Double tmb,

        @JsonProperty("gasto_diario")
        Double gastoDiario,

        @JsonProperty("caloria_alvo")
        Double caloriaAlvo,

        MacronutrientesDTO macronutrientes,

        List<String> recomendacoes,

        @JsonProperty("data_calculo")
        LocalDate dataCalculo,

        @JsonProperty("validade_dias")
        Integer validadeDias,

        @JsonProperty("explicacao_calculo")
        String explicacaoCalculo
) { }