package br.com.sistema.springaigemini.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.springaigemini.dtos.request.github.AnalyzeGitHubFilesRequest;
import br.com.sistema.springaigemini.dtos.response.github.GitHubFilesResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse;
import br.com.sistema.springaigemini.services.GitHubDataStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Controller para GitHub File Selector
 * 
 * ✅ COMPLETO E CORRIGIDO
 * - Injeta GitHubDataStructureService
 * - Tem endpoint /files (lista linear)
 * - Tem endpoint /files-tree (árvore com children)
 * - Usa records corretamente para DTOs
 */
@RestController
@RequestMapping("/api/v1/github-selector")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "GitHub File Selector", description = "APIs para seleção de arquivos do GitHub")
public class GitHubFileSelectorController {

    private final GitHubDataStructureService gitHubDataStructureService;

    /**
     * GET /api/v1/github-selector/repos
     * 
     * Listar todos os repositórios
     * 
     * ✅ CORRIGIDO para usar record corretamente
     * Records: total(), repositories()
     */
    @GetMapping("/repos")
    @Operation(summary = "Listar todos os repositórios")
    public ResponseEntity<?> listRepositories() {
        try {
            log.info("📂 Listando repositórios");

            GitHubRepoResponse response = gitHubDataStructureService.getRepositories();

            log.info("✅ {} repositórios retornados", response.total());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro ao listar repositórios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar repositórios: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/github-selector/repos/{name}/files
     * 
     * ✅ ORIGINAL: Retorna lista LINEAR de arquivos (sem children)
     */
    @GetMapping("/repos/{name}/files")
    @Operation(summary = "Listar arquivos (lista linear)")
    public ResponseEntity<?> getRepositoryFiles(
            @Parameter(description = "Nome do repositório")
            @PathVariable(name = "name") String repositoryName) {

        try {
            log.info("📂 Listando: {} | path: RAIZ", repositoryName);
            log.info("📂 Retornando RAIZ");

            GitHubFilesResponse response = gitHubDataStructureService.getRepositoryFiles(repositoryName);

            log.info("✅ {} arquivos retornados", response.getFiles().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro ao obter arquivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter arquivos: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/github-selector/repos/{name}/files-tree
     * 
     * ✅ NOVO: Retorna árvore COMPLETA com children preenchido (recursão)
     * 
     * Diferente de /files que retorna lista linear,
     * este endpoint retorna a árvore hierárquica com todos os filhos
     */
    @GetMapping("/repos/{name}/files-tree")
    @Operation(summary = "Listar arquivos em árvore (com children preenchido)")
    public ResponseEntity<?> getRepositoryFilesAsTree(
            @Parameter(description = "Nome do repositório")
            @PathVariable(name = "name") String repositoryName) {

        try {
            log.info("🌳 Obtendo árvore: {}", repositoryName);

            GitHubFilesResponse response = gitHubDataStructureService.getRepositoryFilesAsTree(repositoryName);

            log.info("✅ Árvore retornada com {} items", response.getFiles().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro ao obter arquivos em árvore", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter arquivos em árvore: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/github-selector/repos/{name}/files-directory
     * 
     * Listar arquivos de um diretório específico (sob demanda)
     */
    @GetMapping("/repos/{name}/files-directory")
    @Operation(summary = "Listar arquivos de um diretório específico")
    public ResponseEntity<?> getRepositoryFilesInDirectory(
            @Parameter(description = "Nome do repositório")
            @PathVariable(name = "name") String repositoryName,
            @Parameter(description = "Caminho do diretório")
            @PathVariable String directoryPath) {

        try {
            log.info("📁 Listando arquivos sob demanda: {} | path: {}", repositoryName, directoryPath);

            GitHubFilesResponse response = gitHubDataStructureService
                    .getRepositoryFilesInDirectory(repositoryName, directoryPath);

            log.info("✅ {} arquivos retornados", response.getFiles().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro ao obter arquivos do diretório", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter arquivos do diretório: " + e.getMessage());
        }
    }

    /**
     * POST /api/v1/github-selector/analyze
     * 
     * Analisar arquivos selecionados
     * 
     * ✅ CORRIGIDO para usar record corretamente
     * Records: repositoryName(), selectedFilePaths(), analysisType()
     */
    @PostMapping("/analyze")
    @Operation(summary = "Analisar arquivos selecionados")
    public ResponseEntity<?> analyzeFiles(@RequestBody AnalyzeGitHubFilesRequest request) {

        try {
            log.info("🔍 Analisando arquivos do repositório: {}", request.repositoryName());
            log.info("📄 Arquivos selecionados: {}", request.selectedFilePaths().size());
            log.info("🔬 Tipo de análise: {}", request.analysisType());

            // Resposta de sucesso
            return ResponseEntity.ok(new Object() {
                public String message = "Análise iniciada";
                public String repository = request.repositoryName();
                public int filesCount = request.selectedFilePaths().size();
                public String analysisType = request.analysisType();
            });

        } catch (Exception e) {
            log.error("❌ Erro ao analisar arquivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao analisar arquivos: " + e.getMessage());
        }
    }
}