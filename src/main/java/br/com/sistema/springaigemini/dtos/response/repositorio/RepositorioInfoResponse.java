package br.com.sistema.springaigemini.dtos.response.repositorio;

/**
 * Response para cada repositório
 */
public record RepositorioInfoResponse(
    Integer id,
    String nome,
    String descricao,
    String url,
    String linguagem,
    Integer stars,
    Integer forks,
    Integer issuesAbertas,
    String dataCriacao,
    String dataAtualizacao,
    Boolean privado
) {}