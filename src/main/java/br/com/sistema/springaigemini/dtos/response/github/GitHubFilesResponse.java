package br.com.sistema.springaigemini.dtos.response.github;

import java.util.List;

/**
 * Response para GET /api/v1/github-selector/repos/{name}/files
 * 
 * Retorna a árvore hierárquica completa de arquivos de um repositório
 */
public class GitHubFilesResponse {
    
    private String repositoryName;  // Nome do repositório
    private Integer totalFiles;      // Total de arquivos (não pastas)
    private List<FileNode> files;    // Raiz da árvore de arquivos
    
    // ==================== CONSTRUTORES ====================
    
    public GitHubFilesResponse() {}
    
    public GitHubFilesResponse(String repositoryName, Integer totalFiles, List<FileNode> files) {
        this.repositoryName = repositoryName;
        this.totalFiles = totalFiles;
        this.files = files;
    }
    
    // ==================== GETTERS E SETTERS ====================
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public Integer getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public List<FileNode> getFiles() {
        return files;
    }
    
    public void setFiles(List<FileNode> files) {
        this.files = files;
    }
    
    @Override
    public String toString() {
        return "GitHubFilesResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalFiles=" + totalFiles +
                ", files=" + (files != null ? files.size() : 0) +
                '}';
    }
}