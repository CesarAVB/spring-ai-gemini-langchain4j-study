package br.com.sistema.springaigemini.tools;

import java.io.IOException;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.sistema.springaigemini.core.AssistantTool;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools para o assistente GitHub usando GitHub API Library (oficial).
 * 
 * ✅ FINAL: Todos os métodos retornam formato parseável para o frontend
 * ✅ NOVO: listRepositoryFilesRecursively() para recursão completa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GithubAssistantTools implements AssistantTool {

	@Value("${github.token}")
	private String githubToken;

	@Value("${github.username}")
	private String githubUsername;

	private GitHub github;

	/**
	 * Conecta ao GitHub (lazy initialization) com validação
	 */
	private GitHub getGitHub() throws IOException {
		if (github == null) {
			if (githubToken == null || githubToken.isEmpty()) {
				throw new IOException("❌ github.token não configurado em application.properties");
			}
			if (githubUsername == null || githubUsername.isEmpty()) {
				throw new IOException("❌ github.username não configurado em application.properties");
			}

			log.info("🔐 Conectando ao GitHub com usuário: {}", githubUsername);
			github = GitHub.connectUsingOAuth(githubToken);
			log.info("✅ Conectado ao GitHub");
		}
		return github;
	}

	@Override
	public String getToolName() {
		return "GithubTools";
	}

	@Override
	public String getToolDescription() {
		return "Ferramentas para gerenciar repositórios do GitHub";
	}

	/**
	 * Lista todos os repositórios do usuário ✅ Retorna formato parseável:
	 * name|description|url|language|stars|forks|isPrivate
	 */
	@Tool("Lista todos os repositórios do usuário no GitHub")
	public String listRepositories() {
		try {
			log.info("📂 Listando repositórios do usuário: {}", githubUsername);

			GitHub gh = getGitHub();
			List<GHRepository> repos = gh.getUser(githubUsername).listRepositories().toList();

			if (repos.isEmpty()) {
				log.warn("⚠️ Nenhum repositório encontrado");
				return "";
			}

			StringBuilder sb = new StringBuilder();

			for (GHRepository repo : repos) {
				String name = repo.getName();
				String description = repo.getDescription() != null ? repo.getDescription() : "";
				String url = repo.getHtmlUrl().toString();
				String language = repo.getLanguage() != null ? repo.getLanguage() : "N/A";
				int stars = repo.getStargazersCount();
				int forks = repo.getForksCount();
				boolean isPrivate = repo.isPrivate();

				sb.append(name).append("|").append(description).append("|").append(url).append("|").append(language)
						.append("|").append(stars).append("|").append(forks).append("|").append(isPrivate).append("\n");
			}

			log.info("✅ {} repositórios retornados (formato parseável)", repos.size());
			return sb.toString();

		} catch (IOException e) {
			log.error("❌ Erro ao listar repositórios", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Lista arquivos da RAIZ de um repositório ✅ Retorna formato parseável:
	 * type|name|path|size
	 */
	@Tool("Lista todos os arquivos na raiz de um repositório")
	public String listRepositoryFiles(String repositoryName) {
		try {
			log.info("📂 Listando arquivos do repositório: {}", repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				log.error("❌ repositoryName é nulo ou vazio");
				return "";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				log.error("❌ Repositório não encontrado: {}", repositoryName);
				return "";
			}

			List<GHContent> contents = repo.getDirectoryContent("");

			if (contents == null || contents.isEmpty()) {
				log.info("⚠️ Repositório vazio: {}", repositoryName);
				return "";
			}

			StringBuilder sb = new StringBuilder();

			for (GHContent content : contents) {
				String type = content.isDirectory() ? "directory" : "file";
				String name = content.getName();
				String path = content.getPath();
				long size = content.getSize();

				sb.append(type).append("|").append(name).append("|").append(path).append("|").append(size).append("\n");
			}

			log.info("✅ {} arquivos listados de: {}", contents.size(), repositoryName);
			return sb.toString();

		} catch (IOException e) {
			log.error("❌ Erro ao listar arquivos de: {}", repositoryName, e);
			return "";
		} catch (Exception e) {
			log.error("❌ Erro inesperado ao listar arquivos", e);
			return "";
		}
	}

	/**
	 * ✅ Lista arquivos de um DIRETÓRIO ESPECÍFICO (sob demanda) Retorna formato
	 * parseável: type|name|path|size
	 */
	@Tool("Lista arquivos dentro de um diretório específico do repositório")
	public String listRepositoryFilesInDirectory(String repositoryName, String directoryPath) {
		try {
			log.info("📁 Listando arquivos sob demanda: {} | path: {}", repositoryName, directoryPath);

			if (repositoryName == null || repositoryName.isBlank()) {
				return "";
			}

			if (directoryPath == null || directoryPath.isBlank()) {
				log.warn("Path vazio, listando raiz");
				return listRepositoryFiles(repositoryName);
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				log.error("❌ Repositório não encontrado: {}", repositoryName);
				return "";
			}

			// ✅ Passa o directoryPath para a API
			List<GHContent> contents = repo.getDirectoryContent(directoryPath);

			if (contents == null || contents.isEmpty()) {
				log.warn("Diretório vazio: {}", directoryPath);
				return "";
			}

			// ✅ IMPORTANTE: Converter para ArrayList (lista original é imutável)
			List<GHContent> mutableContents = new java.util.ArrayList<>(contents);

			// ✅ Ordena: pastas primeiro, depois arquivos
			mutableContents
					.sort(Comparator.comparing((GHContent c) -> !c.isDirectory()).thenComparing(GHContent::getName));

			StringBuilder sb = new StringBuilder();

			// ✅ Formato parseável: type|name|path|size
			for (GHContent content : mutableContents) {
				String type = content.isDirectory() ? "directory" : "file";
				String name = content.getName();
				String path = content.getPath();
				long size = content.getSize();

				sb.append(type).append("|").append(name).append("|").append(path).append("|").append(size).append("\n");
			}

			log.info("✅ {} itens listados em: {}", mutableContents.size(), directoryPath);
			return sb.toString();

		} catch (Exception e) {
			log.error("❌ Erro ao listar diretório: {}", directoryPath, e);
			return "";
		}
	}

	/**
	 * ✅ NOVO: Lista arquivos COM RECURSÃO COMPLETA
	 * 
	 * Diferente de listRepositoryFiles() que retorna só a RAIZ,
	 * este método busca recursivamente o conteúdo de TODAS as pastas
	 * 
	 * Retorna formato parseável:
	 * type|name|path|size
	 */
	@Tool("Lista arquivos com recursão completa - todos os filhos de todas as pastas")
	public String listRepositoryFilesRecursively(String repositoryName) {
		try {
			log.info("🌳 Listando arquivos RECURSIVAMENTE: {}", repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				return "";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				log.error("❌ Repositório não encontrado: {}", repositoryName);
				return "";
			}

			StringBuilder result = new StringBuilder();
			listFilesRecursive(repo, "", result, "");

			log.info("✅ Recursão concluída para: {}", repositoryName);
			return result.toString();

		} catch (IOException e) {
			log.error("❌ Erro ao listar recursivamente", e);
			return "";
		}
	}

	/**
	 * Método privado recursivo que percorre todas as pastas
	 * 
	 * @param repo Repositório
	 * @param path Caminho atual (vazio para raiz)
	 * @param result StringBuilder para acumular resultado
	 * @param indent Indentação para debug
	 */
	private void listFilesRecursive(GHRepository repo, String path, StringBuilder result, String indent)
			throws IOException {
		try {
			log.debug("{}📂 Listando: {}", indent, path.isEmpty() ? "RAIZ" : path);

			// Obter conteúdo do diretório
			List<GHContent> contents;
			if (path.isEmpty()) {
				// Raiz
				contents = repo.getDirectoryContent("");
			} else {
				// Subdiretório
				contents = repo.getDirectoryContent(path);
			}

			if (contents == null || contents.isEmpty()) {
				return;
			}

			log.debug("{}  ↳ {} itens encontrados", indent, contents.size());

			// Processar cada item
			for (GHContent content : contents) {
				String itemPath = content.getPath();
				String itemName = content.getName();
				long size = content.getSize();

				if (content.isDirectory()) {
					// ✅ É uma pasta - adicionar formato
					result.append("directory|").append(itemName).append("|").append(itemPath).append("|")
							.append(size).append("\n");
					log.debug("{}  ├─ 📁 {}", indent, itemName);

					// ✅ RECURSÃO: Buscar conteúdo desta pasta
					listFilesRecursive(repo, itemPath, result, indent + "    ");

				} else {
					// É um arquivo - adicionar
					result.append("file|").append(itemName).append("|").append(itemPath).append("|").append(size)
							.append("\n");
					log.debug("{}  ├─ 📄 {} ({}bytes)", indent, itemName, size);
				}
			}

		} catch (IOException e) {
			log.warn("⚠️ {}Erro ao listar: {}", indent, e.getMessage());
		}
	}

	/**
	 * Obtém informações detalhadas de um repositório
	 */
	@Tool("Obtém informações detalhadas de um repositório específico")
	public String getRepositoryInfo(String repositoryName) {
		try {
			log.info("📊 Obtendo info do repositório: {}", repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				return "❌ Erro: Nome do repositório não pode estar vazio";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				log.error("❌ Repositório não encontrado: {}", repositoryName);
				return String.format("❌ Repositório '%s' não encontrado", repositoryName);
			}

			String name = repo.getName();
			String description = repo.getDescription() != null ? repo.getDescription() : "Sem descrição";
			String language = repo.getLanguage() != null ? repo.getLanguage() : "N/A";
			int stars = repo.getStargazersCount();
			int forks = repo.getForksCount();
			int issues = repo.getOpenIssueCount();
			String htmlUrl = repo.getHtmlUrl().toString();
			boolean isPrivate = repo.isPrivate();
			String createdAt = repo.getCreatedAt().toString();
			String updatedAt = repo.getUpdatedAt().toString();

			return String.format(
					"📚 **INFORMAÇÕES DO REPOSITÓRIO**\n\n" + "**Nome:** %s\n" + "**Descrição:** %s\n" + "**URL:** %s\n"
							+ "**Status:** %s\n" + "**Linguagem:** %s\n" + "**⭐ Stars:** %d\n" + "**🍴 Forks:** %d\n"
							+ "**📋 Issues Abertas:** %d\n" + "**📅 Criado em:** %s\n" + "**🔄 Atualizado em:** %s",
					name, description, htmlUrl, isPrivate ? "Privado" : "Público", language, stars, forks, issues,
					createdAt, updatedAt);

		} catch (IOException e) {
			log.error("❌ Erro ao obter informações", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Lê conteúdo de um arquivo
	 */
	@Tool("Lê o conteúdo de um arquivo específico do repositório")
	public String readFile(String repositoryName, String filePath) {
		try {
			log.info("📖 Lendo arquivo: {} de {}", filePath, repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				return "❌ Erro: Nome do repositório não pode estar vazio";
			}
			if (filePath == null || filePath.trim().isEmpty()) {
				return "❌ Erro: Caminho do arquivo não pode estar vazio";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				return String.format("❌ Repositório '%s' não encontrado", repositoryName);
			}

			GHContent content = repo.getFileContent(filePath);

			if (content == null) {
				return String.format("❌ Arquivo '%s' não encontrado", filePath);
			}

			String decodedContent = content.getContent();

			return String.format("📄 **CONTEÚDO DE: %s**\n\n" + "```\n%s\n```", filePath, decodedContent);

		} catch (IOException e) {
			log.error("❌ Erro ao ler arquivo", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Cria um arquivo no repositório
	 */
	@Tool("Cria um novo arquivo no repositório")
	public String createFile(String repositoryName, String filePath, String content, String message) {
		try {
			log.info("✏️ Criando arquivo: {} em {}", filePath, repositoryName);

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				return String.format("❌ Repositório '%s' não encontrado", repositoryName);
			}

			String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

			repo.createContent().content(encodedContent).path(filePath).message(message).commit();

			log.info("✅ Arquivo criado com sucesso");
			return String.format("✅ **ARQUIVO CRIADO COM SUCESSO**\n\n" + "**Arquivo:** %s\n" + "**Repositório:** %s\n"
					+ "**Mensagem:** %s", filePath, repositoryName, message);

		} catch (IOException e) {
			log.error("❌ Erro ao criar arquivo", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Lista todas as issues abertas
	 */
	@Tool("Lista todas as issues abertas de um repositório")
	public String listIssues(String repositoryName) {
		try {
			log.info("🐛 Listando issues de: {}", repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				return "❌ Erro: Nome do repositório não pode estar vazio";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				return String.format("❌ Repositório '%s' não encontrado", repositoryName);
			}

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("🐛 **ISSUES ABERTAS DE: %s**\n\n", repositoryName));

			int count = 0;
			for (org.kohsuke.github.GHIssue issue : repo.getIssues(GHIssueState.OPEN)) {
				sb.append(String.format("#%d - %s\n", issue.getNumber(), issue.getTitle()));
				count++;
				if (count >= 20)
					break;
			}

			if (count == 0) {
				return "✅ Nenhuma issue aberta.";
			}

			log.info("✅ {} issues listadas", count);
			return sb.toString();

		} catch (IOException e) {
			log.error("❌ Erro ao listar issues", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Obtém estatísticas de linguagens
	 */
	@Tool("Obtém estatísticas de linguagens de um repositório")
	public String getRepositoryStats(String repositoryName) {
		try {
			log.info("📊 Obtendo stats de: {}", repositoryName);

			if (repositoryName == null || repositoryName.trim().isEmpty()) {
				return "❌ Erro: Nome do repositório não pode estar vazio";
			}

			GitHub gh = getGitHub();
			GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

			if (repo == null) {
				return String.format("❌ Repositório '%s' não encontrado", repositoryName);
			}

			var languages = repo.listLanguages();

			if (languages.isEmpty()) {
				return "📭 Nenhuma linguagem detectada.";
			}

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("📊 **ESTATÍSTICAS DE LINGUAGEM: %s**\n\n", repositoryName));

			long totalBytes = 0;
			for (long bytes : languages.values()) {
				totalBytes += bytes;
			}

			for (var entry : languages.entrySet()) {
				String lang = entry.getKey();
				long bytes = entry.getValue();
				double percent = (bytes * 100.0) / totalBytes;

				sb.append(String.format("%s: %.1f%%\n", lang, percent));
			}

			log.info("✅ Stats obtidas");
			return sb.toString();

		} catch (IOException e) {
			log.error("❌ Erro ao obter stats", e);
			return formatErrorResponse(e);
		}
	}

	/**
	 * Formata mensagem de erro padronizada
	 */
	private String formatErrorResponse(IOException e) {
		String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";

		if (errorMsg.contains("401") || errorMsg.contains("Unauthorized")) {
			return "❌ Erro de autenticação:\n" + "- Token GitHub inválido ou expirado\n"
					+ "- Verifique github.token em application.properties";
		}

		if (errorMsg.contains("404") || errorMsg.contains("Not Found")) {
			return "❌ Recurso não encontrado:\n" + "- Repositório não existe\n"
					+ "- Você não tem permissão para acessá-lo";
		}

		if (errorMsg.contains("403") || errorMsg.contains("Forbidden")) {
			return "❌ Acesso negado:\n" + "- Token sem permissão suficiente\n" + "- Repositório pode ser privado";
		}

		return "❌ Erro: " + errorMsg;
	}
}