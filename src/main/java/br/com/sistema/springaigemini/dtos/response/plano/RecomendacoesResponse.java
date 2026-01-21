package br.com.sistema.springaigemini.dtos.response.plano;

/**
 * Response para recomendações
 */
public record RecomendacoesResponse(
    Integer caloricoDiario,
    Integer proteina,
    Integer carboidratos,
    Integer gordura,
    Integer refeicoes,
    String exercicios
) {}