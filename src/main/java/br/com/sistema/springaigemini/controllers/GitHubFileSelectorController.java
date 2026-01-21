package br.com.sistema.springaigemini.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.springaigemini.dtos.request.github.AnalyzeGitHubFilesRequest;
import br.com.sistema.springaigemini.dtos.response.common.RespostaAssistente;
import br.com.sistema.springaigemini.dtos.response.github.GitHubFilesResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse;
import br.com.sistema.springaigemini.enums.TipoResposta;
import br.com.sistema.springaigemini.services.AssistantGithubService;
import br.com.sistema.springaigemini.services.GitHubDataStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Controller para seleção e análise de arquivos GitHub
 * 
 * Endpoints:
 * - GET /api/v1/github-selector/repos - Lista repositórios
 * - GET /api/v1/github-selector/repos/{name}/files - Lista arquivos (raiz ou diretório)
 * - POST /api/v1/github-selector/analyze - Analisa arquivos
 */
@RestController
@RequestMapping("/api/v1/github-selector")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "GitHub File Selector")
public class GitHubFileSelectorController {

	private final GitHubDataStructureService dataStructureService;
	private final AssistantGithubService githubAssistantService;

	/**
	 * GET /api/v1/github-selector/repos
	 * Lista repositórios para dropdown
	 */
	@GetMapping("/repos")
	@Operation(summary = "Listar repositórios")
	public ResponseEntity<GitHubRepoResponse> listRepositories() {
		log.info("🚀 Listando repositórios");
		try {
			GitHubRepoResponse response = dataStructureService.getRepositories();
			log.info("✅ {} repositórios retornados", response.total());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("❌ Erro ao listar repositórios", e);
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * GET /api/v1/github-selector/repos/{name}/files
	 * 
	 * ✅ ATUALIZADO: Suporta carregamento sob demanda
	 * 
	 * Sem path: Retorna RAIZ
	 * Com path: Retorna conteúdo do DIRETÓRIO
	 * 
	 * Exemplos:
	 * - GET /repos/meu-repo/files
	 *   → Retorna raiz
	 * - GET /repos/meu-repo/files?path=src
	 *   → Retorna conteúdo de src/
	 * - GET /repos/meu-repo/files?path=src/main/java
	 *   → Retorna conteúdo de src/main/java/
	 */
	@GetMapping("/repos/{name}/files")
	@Operation(summary = "Listar arquivos (raiz ou diretório sob demanda)")
	public ResponseEntity<GitHubFilesResponse> listRepositoryFiles(
			@Parameter(description = "Nome do repositório") 
			@PathVariable("name") String repositoryName,
			
			@Parameter(description = "Caminho do diretório (opcional, padrão=raiz)") 
			@RequestParam(value = "path", defaultValue = "", required = false) String path) {

		log.info("📂 Listando: {} | path: {}", repositoryName, path.isEmpty() ? "RAIZ" : path);

		try {
			if (repositoryName == null || repositoryName.isBlank()) {
				log.warn("❌ Nome do repositório vazio");
				return ResponseEntity.badRequest().build();
			}

			GitHubFilesResponse response;

			// ✅ IMPORTANTE: Verificar se é raiz ou diretório específico
			if (path == null || path.trim().isEmpty()) {
				// RAIZ
				log.info("📂 Retornando RAIZ");
				response = dataStructureService.getRepositoryFiles(repositoryName);
			} else {
				// DIRETÓRIO (sob demanda)
				log.info("📁 Retornando diretório: {}", path);
				response = dataStructureService.getRepositoryFilesInDirectory(repositoryName, path);
			}

			log.info("✅ {} arquivos retornados", response.getTotalFiles());
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("❌ Erro ao listar arquivos", e);
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * POST /api/v1/github-selector/analyze
	 * Analisa arquivos selecionados com IA
	 */
	@PostMapping("/analyze")
	@Operation(summary = "Analisar arquivos selecionados")
	public ResponseEntity<RespostaAssistente> analyzeSelectedFiles(
			@RequestBody AnalyzeGitHubFilesRequest request) {

		log.info("🔍 Analisando {} arquivos de: {}", 
			request.selectedFilePaths().size(), request.repositoryName());

		try {
			// Validações
			if (request == null || request.repositoryName() == null || request.repositoryName().isBlank()) {
				log.warn("❌ Repository name vazio");
				return ResponseEntity.badRequest().body(
					RespostaAssistente.erro("GitHubSelector", "REPO_INVALIDO", "Repository obrigatório", 400)
				);
			}

			if (request.selectedFilePaths() == null || request.selectedFilePaths().isEmpty()) {
				log.warn("❌ Nenhum arquivo selecionado");
				return ResponseEntity.badRequest().body(
					RespostaAssistente.erro("GitHubSelector", "NENHUM_ARQUIVO", "Selecione um arquivo", 400)
				);
			}

			// Construir prompt com os arquivos selecionados
			StringBuilder filesContent = new StringBuilder();
			filesContent.append("Análise solicitada para arquivos de '")
					.append(request.repositoryName()).append("':\n\n");

			for (String filePath : request.selectedFilePaths()) {
				filesContent.append("📄 ").append(filePath).append(":\n");
				String content = dataStructureService.readFileContent(request.repositoryName(), filePath);
				filesContent.append(content).append("\n\n");
			}

			filesContent.append("Tipo de análise: ").append(request.analysisType());

			// Enviar para o assistente
			String analysisResult = githubAssistantService.processMessage(filesContent.toString());

			log.info("✅ Análise realizada");
			return ResponseEntity.ok(
				RespostaAssistente.sucesso("GitHubSelector", TipoResposta.TEXTO.getValor(),
					"Análise: " + request.analysisType(), analysisResult)
			);

		} catch (Exception e) {
			log.error("❌ Erro ao analisar arquivos", e);
			return ResponseEntity.status(500).body(
				RespostaAssistente.erro("GitHubSelector", "ERRO_ANALISE", 
					"Erro ao analisar: " + e.getMessage(), 500)
			);
		}
	}
}