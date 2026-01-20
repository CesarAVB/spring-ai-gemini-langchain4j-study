package br.com.sistema.springaigemini.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * DTO para receber dados da AvaliacaoFisica.
 * 
 * Este DTO é usado para comunicação com o microserviço de nutrição.
 * Contém as medidas necessárias para cálculo do plano nutricional.
 */
public record AvaliacaoFisicaDTO(
        Long id,

        @JsonProperty("peso_atual")
        Double pesoAtual,

        @JsonProperty("percentual_gordura")
        Double percentualGordura,

        @JsonProperty("massa_magra")
        Double massaMagra,

        @JsonProperty("massa_gorda")
        Double massaGorda,

        Double imc,

        @JsonProperty("data_avaliacao")
        LocalDate dataAvaliacao
) { }