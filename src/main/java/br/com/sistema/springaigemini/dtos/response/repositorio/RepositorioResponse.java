package br.com.sistema.springaigemini.dtos.response.repositorio;

import java.util.List;

/**
 * Response para dados de repositórios GitHub
 */
public record RepositorioResponse(
    Integer total,
    List<RepositorioInfoResponse> repositorios
) {}