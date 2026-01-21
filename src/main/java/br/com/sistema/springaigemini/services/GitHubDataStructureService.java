package br.com.sistema.springaigemini.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.dtos.response.github.GitHubFilesResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubFilesResponse.FileNode;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse;
import br.com.sistema.springaigemini.dtos.response.github.GitHubRepoResponse.RepoInfo;
import br.com.sistema.springaigemini.tools.GithubAssistantTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Service para estrutura de dados do GitHub
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class GitHubDataStructureService {

	private final GithubAssistantTools githubTools;

	public GitHubRepoResponse getRepositories() {
		log.info("📂 Buscando repositórios");
		String response = githubTools.listRepositories();
		return parseRepositories(response);
	}

	public GitHubFilesResponse getRepositoryFiles(String repositoryName) {
		log.info("📂 Buscando arquivos da RAIZ: {}", repositoryName);
		String response = githubTools.listRepositoryFiles(repositoryName);
		return parseFiles(response, repositoryName);
	}

	public GitHubFilesResponse getRepositoryFilesInDirectory(String repositoryName, String directoryPath) {
		log.info("📁 Buscando arquivos sob demanda: {} | path: {}", repositoryName, directoryPath);
		String response = githubTools.listRepositoryFilesInDirectory(repositoryName, directoryPath);
		return parseFiles(response, repositoryName);
	}

	public String readFileContent(String repositoryName, String filePath) {
		log.info("📖 Lendo arquivo: {} | {}", repositoryName, filePath);
		return githubTools.readFile(repositoryName, filePath);
	}

	// ==================== PARSE METHODS ====================

	/**
	 * ✅ Parse repositórios
	 * 
	 * Formato esperado:
	 * "name|description|url|language|stars|forks|isPrivate\n"
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
				if (line.trim().isEmpty()) continue;
				
				// ✅ Usar split com limite: pega só os primeiros 5 campos
				// Porque descrição pode ter | dentro
				String[] parts = line.split("\\|", 5);
				if (parts.length < 5) continue;
				
				try {
					String name = parts[0].trim();
					String description = parts[1].trim();
					String url = parts[2].trim();
					String language = parts[3].trim();
					
					// ✅ Último campo pode ter mais | dentro, então agrupa tudo
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

	private GitHubFilesResponse parseFiles(String response, String repositoryName) {
		log.debug("Parseando arquivos de: {}", repositoryName);
		log.debug("Response recebida: {}", response);  // ✅ VER O QUE CHEGOU
		
		List<FileNode> files = new ArrayList<>();
		
		try {
			if (response == null || response.isEmpty()) {
				log.warn("Resposta vazia para: {}", repositoryName);
				return GitHubFilesResponse.builder()
					.repositoryName(repositoryName)
					.files(files)
					.totalFiles(0)
					.build();
			}

			// Parse linha por linha
			String[] lines = response.split("\n");
			log.debug("Total de linhas: {}", lines.length);  // ✅ VER QUANTAS LINHAS
			
			for (String line : lines) {
				if (line.trim().isEmpty()) continue;
				
				log.debug("Parseando linha: {}", line);  // ✅ VER CADA LINHA
				
				// ✅ Usar split com limite: pega só os primeiros 4 campos
				String[] parts = line.split("\\|", 4);
				log.debug("Parts: {}", java.util.Arrays.toString(parts));  // ✅ VER O SPLIT
				
				if (parts.length < 3) {
					log.debug("Linha ignorada (menos de 3 partes)");
					continue;
				}
				
				String type = parts[0].trim();
				String name = parts[1].trim();
				String path = parts[2].trim();
				
				log.debug("type={}, name={}, path={}", type, name, path);  // ✅ VER OS VALORES
				
				FileNode node = null;
				
				if ("directory".equals(type)) {
					node = FileNode.ofDirectory(name, path);
				} else if ("file".equals(type)) {
					String extension = getExtension(name);
					node = FileNode.ofFile(name, path, extension);
					
					if (parts.length > 3) {
						try {
							node.setSize(Long.parseLong(parts[3].trim()));
						} catch (NumberFormatException e) {
							log.debug("Tamanho inválido para: {}", name);
						}
					}
				}
				
				if (node != null) {
					log.debug("Node criado: {}", node.getName());  // ✅ VER NODES
					files.add(node);
				} else {
					log.debug("Node não foi criado para: {}", name);  // ✅ VER POR QUÊ
				}
			}
			
			// Ordenar: pastas primeiro, depois arquivos
			files.sort(Comparator
				.comparing(FileNode::isDirectory).reversed()
				.thenComparing(FileNode::getName)
			);
			
			log.info("✅ {} arquivos parseados", files.size());
			
		} catch (Exception e) {
			log.error("❌ Erro ao fazer parse dos arquivos", e);
		}
		
		return GitHubFilesResponse.builder()
			.repositoryName(repositoryName)
			.files(files)
			.totalFiles(files.size())
			.build();
	}

	private String getExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) return null;
		
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0 && lastDot < fileName.length() - 1) {
			return fileName.substring(lastDot);
		}
		return null;
	}
}