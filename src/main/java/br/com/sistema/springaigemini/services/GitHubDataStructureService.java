package br.com.sistema.springaigemini.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.dtos.response.github.FileNode;
import br.com.sistema.springaigemini.dtos.response.github.GitHubFilesResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse.RepoInfo;
import br.com.sistema.springaigemini.tools.GithubAssistantTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Service para estrutura de dados do GitHub
 * 
 * ✅ VERSÃO FINAL - Usando setters em vez de constructor
 * - Compatível com qualquer estrutura de GitHubFilesResponse
 * - Usa setters para setar valores (mais seguro)
 * - Método getRepositoryFilesAsTree() com recursão completa
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class GitHubDataStructureService {

	private final GithubAssistantTools githubTools;

	/**
	 * Obter lista de repositórios
	 */
	public GitHubRepoResponse getRepositories() {
		log.info("📂 Buscando repositórios");
		String response = githubTools.listRepositories();
		return parseRepositories(response);
	}

	/**
	 * Obter lista linear de arquivos (sem children)
	 */
	public GitHubFilesResponse getRepositoryFiles(String repositoryName) {
		log.info("📂 Buscando arquivos da RAIZ: {}", repositoryName);
		String response = githubTools.listRepositoryFiles(repositoryName);
		return parseFiles(response, repositoryName);
	}

	/**
	 * Obter arquivos de um diretório específico
	 */
	public GitHubFilesResponse getRepositoryFilesInDirectory(String repositoryName, String directoryPath) {
		log.info("📁 Buscando arquivos sob demanda: {} | path: {}", repositoryName, directoryPath);
		String response = githubTools.listRepositoryFilesInDirectory(repositoryName, directoryPath);
		return parseFiles(response, repositoryName);
	}

	/**
	 * ✅ NOVO: Obter árvore completa com children preenchido (recursão)
	 * 
	 * Este método:
	 * 1. Obtém lista linear de todos os arquivos
	 * 2. Constrói a árvore com children recursivamente
	 * 3. Retorna apenas os nós raiz
	 */
	public GitHubFilesResponse getRepositoryFilesAsTree(String repositoryName) {
		log.info("🌳 Obtendo árvore completa (com children) do repositório: {}", repositoryName);

		// Primeiro, obter lista linear de todos os arquivos
		//GitHubFilesResponse flatFiles = getRepositoryFiles(repositoryName);
		String responseRepository = githubTools.listRepositoryFilesRecursively(repositoryName);
		GitHubFilesResponse flatFiles = parseFiles(responseRepository, repositoryName);
		
		// Depois, converter para árvore com children
		List<FileNode> treeStructure = buildTreeStructure(flatFiles.getFiles());

		// ✅ Criar resposta usando setters (seguro com qualquer estrutura)
		GitHubFilesResponse response = new GitHubFilesResponse();
		response.setRepositoryName(repositoryName);
		response.setFiles(treeStructure);
		response.setTotalFiles(flatFiles.getTotalFiles());
		
		return response;
	}

	/**
	 * Ler conteúdo de um arquivo
	 */
	public String readFileContent(String repositoryName, String filePath) {
		log.info("📖 Lendo arquivo: {} | {}", repositoryName, filePath);
		return githubTools.readFile(repositoryName, filePath);
	}

	// ==================== PARSE METHODS ====================

	/**
	 * ✅ Parse repositórios
	 * 
	 * Formato esperado: "name|description|url|language|stars|forks|isPrivate\n"
	 */
	private GitHubRepoResponse parseRepositories(String response) {
		log.debug("Parseando repositórios");

		List<RepoInfo> repos = new ArrayList<>();

		try {
			if (response == null || response.isEmpty()) {
				log.warn("Resposta vazia ao listar repositórios");
				return new GitHubRepoResponse(0, repos);
			}

			String[] lines = response.split("\n");

			for (String line : lines) {
				if (line.trim().isEmpty())
					continue;

				// ✅ Usar split com limite: pega só os primeiros 5 campos
				String[] parts = line.split("\\|", 5);
				if (parts.length < 5)
					continue;

				try {
					String name = parts[0].trim();
					String description = parts[1].trim();
					String url = parts[2].trim();
					String language = parts[3].trim();

					// ✅ Último campo pode ter mais | dentro
					String lastPart = parts[4].trim();
					String[] lastParts = lastPart.split("\\|");

					int stars = Integer.parseInt(lastParts[0].trim());
					int forks = lastParts.length > 1 ? Integer.parseInt(lastParts[1].trim()) : 0;
					boolean isPrivate = lastParts.length > 2 ? Boolean.parseBoolean(lastParts[2].trim()) : false;

					RepoInfo repo = new RepoInfo(name, description, url, language, stars, forks, isPrivate);
					repos.add(repo);

				} catch (NumberFormatException e) {
					log.debug("Erro ao parsear linha de repositório: {}", line);
				}
			}

			log.info("✅ {} repositórios parseados", repos.size());

		} catch (Exception e) {
			log.error("❌ Erro ao fazer parse dos repositórios", e);
		}

		return new GitHubRepoResponse(repos.size(), repos);
	}

	/**
	 * Parse de arquivos em lista linear
	 */
	private GitHubFilesResponse parseFiles(String response, String repositoryName) {
		log.debug("Parseando arquivos de: {}", repositoryName);
		log.debug("Response recebida: {}", response);

		List<FileNode> files = new ArrayList<>();

		try {
			if (response == null || response.isEmpty()) {
				log.warn("Resposta vazia para: {}", repositoryName);
				// ✅ Usar setters
				GitHubFilesResponse result = new GitHubFilesResponse();
				result.setRepositoryName(repositoryName);
				result.setFiles(new ArrayList<>());
				result.setTotalFiles(0);
				return result;
			}

			// Parse linha por linha
			String[] lines = response.split("\n");
			log.debug("Total de linhas: {}", lines.length);

			for (String line : lines) {
				if (line.trim().isEmpty())
					continue;

				log.debug("Parseando linha: {}", line);

				// ✅ Usar split com limite: pega só os primeiros 4 campos
				String[] parts = line.split("\\|", 4);
				log.debug("Parts: {}", java.util.Arrays.toString(parts));

				if (parts.length < 3) {
					log.debug("Linha ignorada (menos de 3 partes)");
					continue;
				}

				String type = parts[0].trim();
				String name = parts[1].trim();
				String path = parts[2].trim();

				log.debug("type={}, name={}, path={}", type, name, path);

				FileNode node = null;

				if ("folder".equals(type) || "directory".equals(type)) {
					// ✅ Criar pasta (folder ou directory)
					node = new FileNode(name, path, "folder");
					node.setChildren(new ArrayList<>()); // Inicializar com lista vazia
				} else if ("file".equals(type)) {
					// ✅ Criar arquivo (file)
					String extension = FileNode.extractExtension(name);
					node = new FileNode(name, path, "file", extension);

					// Se tiver tamanho, adicionar
					if (parts.length > 3) {
						try {
							node.setSize(Long.parseLong(parts[3].trim()));
						} catch (NumberFormatException e) {
							log.debug("Tamanho inválido para: {}", name);
						}
					}
				}

				if (node != null) {
					log.debug("Node criado: {}", node.getName());
					files.add(node);
				} else {
					log.debug("Node não foi criado para: {}", name);
				}
			}

			// Ordenar: pastas primeiro, depois arquivos
			files.sort((a, b) -> {
				// ✅ Usar isFolder() para verificar tipo
				if (a.isFolder() && !b.isFolder())
					return -1;
				if (!a.isFolder() && b.isFolder())
					return 1;
				// Se mesmo tipo, ordena por nome
				return a.getName().compareTo(b.getName());
			});

			log.info("✅ {} arquivos parseados", files.size());

		} catch (Exception e) {
			log.error("❌ Erro ao fazer parse dos arquivos", e);
		}

		// ✅ Usar setters
		GitHubFilesResponse result = new GitHubFilesResponse();
		result.setRepositoryName(repositoryName);
		result.setFiles(files);
		result.setTotalFiles(files.size());
		return result;
	}

	// ==================== TREE BUILDING METHODS ====================

	/**
	 * Constrói a árvore hierárquica com children a partir de lista linear
	 * 
	 * Algoritmo:
	 * 1. Cria um mapa de todos os nós por path
	 * 2. Para cada nó, encontra seu pai (baseado no path)
	 * 3. Adiciona à lista de children do pai
	 * 4. Retorna apenas os nós raiz (sem pai)
	 */
	private List<FileNode> buildTreeStructure(List<FileNode> flatFiles) {
		log.debug("🔨 Construindo árvore a partir de {} arquivos", flatFiles.size());

		if (flatFiles == null || flatFiles.isEmpty()) {
			return new ArrayList<>();
		}

		// Criar mapa de path -> FileNode para acesso rápido
		java.util.Map<String, FileNode> nodeMap = new java.util.HashMap<>();
		List<FileNode> rootNodes = new ArrayList<>();

		// Primeiro passo: adicionar todos os nós ao mapa
		for (FileNode node : flatFiles) {
			nodeMap.put(node.getPath(), node);
			// Inicializar children como lista vazia se for folder
			if (node.isFolder() && node.getChildren() == null) {
				node.setChildren(new ArrayList<>());
			}
		}

		// Segundo passo: construir a hierarquia
		for (FileNode node : flatFiles) {
			String parentPath = getParentPath(node.getPath());

			if (parentPath == null) {
				// É um nó raiz (sem pai)
				rootNodes.add(node);
				log.debug("  ├─ 📍 Raiz: {}", node.getName());
			} else {
				// Encontrar o pai
				FileNode parent = nodeMap.get(parentPath);

				if (parent != null) {
					// Adicionar este nó como filho do pai
					if (parent.getChildren() == null) {
						parent.setChildren(new ArrayList<>());
					}
					parent.addChild(node);
					log.debug("  ├─ 📍 {} → filho de {}", node.getName(), parent.getName());
				} else {
					// Pai não encontrado, tratar como raiz
					log.warn("  ⚠️  Pai não encontrado para: {} (pai esperado: {})", node.getName(), parentPath);
					rootNodes.add(node);
				}
			}
		}

		log.info("✅ Árvore construída: {} nós raiz", rootNodes.size());
		return rootNodes;
	}

	/**
	 * Obtém o path do pai de um arquivo
	 * 
	 * Exemplos:
	 * - "src/main/java/App.java" → "src/main/java"
	 * - "src/main/java" → "src/main"
	 * - "src" → null
	 * - "App.java" → null
	 */
	private String getParentPath(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}

		int lastSlash = path.lastIndexOf('/');
		if (lastSlash <= 0) {
			return null; // Não tem pai (é raiz)
		}

		return path.substring(0, lastSlash);
	}

	// ==================== UTILITY METHODS ====================

	/**
	 * Extrai a extensão de um arquivo
	 * 
	 * Exemplos:
	 * - "App.java" → "java"
	 * - "README.md" → "md"
	 * - ".gitignore" → null
	 */
	private String getExtension(String fileName) {
		if (fileName == null || fileName.isEmpty())
			return null;

		int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0 && lastDot < fileName.length() - 1) {
			return fileName.substring(lastDot + 1);
		}
		return null;
	}
}