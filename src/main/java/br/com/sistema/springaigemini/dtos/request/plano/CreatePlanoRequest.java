package br.com.sistema.springaigemini.dtos.request.plano;

import java.util.List;

/**
 * Request para criar/atualizar plano nutricional
 * 
 * Estrutura simplificada para bater com PlanoNutricional real
 */
public record CreatePlanoRequest(
    String nome,
    Integer idade,
    Double pesoAtual,
    List<String> recomendacoes,
    String objetivo,
    String intensidadeExercicio
) {}