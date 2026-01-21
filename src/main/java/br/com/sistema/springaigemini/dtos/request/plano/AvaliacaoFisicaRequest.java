package br.com.sistema.springaigemini.dtos.request.plano;

/**
 * Request para avaliação física (entrada)
 */
public record AvaliacaoFisicaRequest(
    Double pesoAtual,
    Double percentualGordura,
    Double massMagra,
    Double massGorda,
    Double imc
) {}