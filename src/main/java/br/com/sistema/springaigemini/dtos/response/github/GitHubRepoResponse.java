package br.com.sistema.springaigemini.dtos.response.github;

import java.util.List;

/**
 * Response com informações estruturadas dos repositórios do usuário.
 * 
 * Usado pelo novo endpoint /api/v1/github/repos para retornar
 * dados parseados e prontos para o frontend usar em dropdowns/selects.
 */
public record GitHubRepoResponse(
    Integer total,
    List<RepoInfo> repositories
) {
    /**
     * Informações básicas de um repositório
     */
    public record RepoInfo(
        String name,
        String description,
        String url,
        String language,
        Integer stars,
        Integer forks,
        Boolean isPrivate
    ) {}
}