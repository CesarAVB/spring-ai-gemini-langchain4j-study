package br.com.sistema.springaigemini.dtos.request.github;

import java.util.List;

/**
 * Request para analisar arquivos selecionados do GitHub.
 * 
 * O frontend envia:
 * - repositório selecionado
 * - lista de arquivos (paths) selecionados
 * - tipo de análise desejada
 * 
 * Backend lê os arquivos e envia para Gemini analisar.
 */
public record AnalyzeGitHubFilesRequest(
    String repositoryName,
    List<String> selectedFilePaths,  // ex: ["src/main/java/App.java", "pom.xml"]
    String analysisType  // ex: "code_review", "security", "performance", "general"
) {}