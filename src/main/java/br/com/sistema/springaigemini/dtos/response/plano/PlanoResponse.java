package br.com.sistema.springaigemini.dtos.response.plano;

import java.util.List;

/**
 * Response para Plano Nutricional
 * 
 * Estrutura simplificada para bater com PlanoNutricional real
 */
public record PlanoResponse(
    Integer id,
    String nome,
    Integer idade,
    Double pesoAtual,
    List<String> recomendacoes,
    String objetivo,
    String intensidadeExercicio
) {}