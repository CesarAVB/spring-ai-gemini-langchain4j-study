package br.com.sistema.springaigemini.dtos.response.plano;

/**
 * Response para avaliação física
 */
public record AvaliacaoFisicaResponse(
    Double pesoAtual,
    Double percentualGordura,
    Double massMagra,
    Double massGorda,
    Double imc,
    String dataAvaliacao
) {}