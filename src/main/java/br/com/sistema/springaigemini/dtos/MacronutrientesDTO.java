package br.com.sistema.springaigemini.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MacronutrientesDTO(
        @JsonProperty("proteina_gramas")
        Double proteinaGramas,

        @JsonProperty("proteina_calorias")
        Double proteinaCalorias,

        @JsonProperty("proteina_percentual")
        Double proteinaPercentual,

        @JsonProperty("carboidrato_gramas")
        Double carboidratoGramas,

        @JsonProperty("carboidrato_calorias")
        Double carboIdratoCalorias,

        @JsonProperty("carboidrato_percentual")
        Double carboidratoPercentual,

        @JsonProperty("gordura_gramas")
        Double gorduraGramas,

        @JsonProperty("gordura_calorias")
        Double gorduraCalorias,

        @JsonProperty("gordura_percentual")
        Double gorduraPercentual
) { }