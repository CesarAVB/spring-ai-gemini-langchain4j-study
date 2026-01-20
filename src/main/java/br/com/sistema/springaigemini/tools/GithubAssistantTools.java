package br.com.sistema.springaigemini.tools;

import java.io.IOException;
import java.util.Base64;
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
 * Operações disponíveis:
 * - Listar repositórios
 * - Obter informações do repositório
 * - Listar arquivos
 * - Ler conteúdo de arquivo
 * - Criar arquivo
 * - Atualizar arquivo
 * - Deletar arquivo
 * - Listar issues abertas
 * - Listar pull requests
 * - Listar commits
 * - Listar branches
 * - Obter estatísticas de linguagem
 * - Gerar README automaticamente
 * 
 * Configuração necessária em application.properties:
 * github.token=seu-token-github
 * github.username=seu-username
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
     * Conecta ao GitHub (lazy initialization)
     */
    private GitHub getGitHub() throws IOException {
        if (github == null) {
            log.info("Conectando ao GitHub com usuário: {}", githubUsername);
            github = GitHub.connectUsingOAuth(githubToken);
        }
        return github;
    }

    @Override
    public String getToolName() {
        return "GithubTools";
    }

    @Override
    public String getToolDescription() {
        return "Ferramentas completas para gerenciar repositórios do GitHub";
    }

    /**
     * Lista todos os repositórios do usuário
     */
    @Tool("Lista todos os repositórios do usuário no GitHub")
    public String listRepositories() {
        try {
            log.info("Listando repositórios do usuário: {}", githubUsername);
            
            GitHub gh = getGitHub();
            List<GHRepository> repos = gh.getUser(githubUsername).listRepositories().toList();

            if (repos.isEmpty()) {
                return "📭 Nenhum repositório encontrado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📚 **SEUS REPOSITÓRIOS NO GITHUB**\n\n");
            
            for (GHRepository repo : repos) {
                String name = repo.getName();
                String description = repo.getDescription() != null ? repo.getDescription() : "Sem descrição";
                String language = repo.getLanguage() != null ? repo.getLanguage() : "N/A";
                int stars = repo.getStargazersCount();
                
                sb.append(String.format(
                    "📦 **%s** ⭐ %d\n" +
                    "   📝 %s\n" +
                    "   💻 %s\n\n",
                    name, stars, description, language
                ));
            }

            log.info("✅ {} repositórios listados", repos.size());
            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar repositórios", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Obtém informações detalhadas de um repositório
     */
    @Tool("Obtém informações detalhadas de um repositório específico")
    public String getRepositoryInfo(String repositoryName) {
        try {
            log.info("Obtendo info do repositório: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

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
                "📚 **INFORMAÇÕES DO REPOSITÓRIO**\n\n" +
                "**Nome:** %s\n" +
                "**Descrição:** %s\n" +
                "**URL:** %s\n" +
                "**Status:** %s\n" +
                "**Linguagem:** %s\n" +
                "**⭐ Stars:** %d\n" +
                "**🍴 Forks:** %d\n" +
                "**📋 Issues Abertas:** %d\n" +
                "**📅 Criado em:** %s\n" +
                "**🔄 Atualizado em:** %s",
                name, description, htmlUrl, 
                isPrivate ? "Privado" : "Público",
                language, stars, forks, issues, createdAt, updatedAt
            );

        } catch (IOException e) {
            log.error("Erro ao obter informações", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista arquivos de um repositório
     */
    @Tool("Lista todos os arquivos na raiz de um repositório")
    public String listRepositoryFiles(String repositoryName) {
        try {
            log.info("Listando arquivos do repositório: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            List<GHContent> contents = repo.getDirectoryContent("");

            if (contents.isEmpty()) {
                return "📭 Nenhum arquivo encontrado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📁 **ARQUIVOS DE: %s**\n\n", repositoryName));
            
            for (GHContent content : contents) {
                String name = content.getName();
                String icon = content.isDirectory() ? "📂" : "📄";
                sb.append(String.format("%s %s\n", icon, name));
            }

            log.info("✅ Arquivos listados");
            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar arquivos", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lê conteúdo de um arquivo
     */
    @Tool("Lê o conteúdo de um arquivo específico do repositório")
    public String readFile(String repositoryName, String filePath) {
        try {
            log.info("Lendo arquivo: {} de {}", filePath, repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            GHContent content = repo.getFileContent(filePath);
            String decodedContent = content.getContent();

            return String.format(
                "📄 **CONTEÚDO DE: %s**\n\n" +
                "```\n%s\n```",
                filePath, decodedContent
            );

        } catch (IOException e) {
            log.error("Erro ao ler arquivo", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Cria um arquivo no repositório
     */
    @Tool("Cria um novo arquivo no repositório")
    public String createFile(String repositoryName, String filePath, String content, String message) {
        try {
            log.info("Criando arquivo: {} em {}", filePath, repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            
            String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());
            
            repo.createContent()
                .content(encodedContent)
                .path(filePath)
                .message(message)
                .commit();

            log.info("✅ Arquivo criado com sucesso");
            return String.format(
                "✅ **ARQUIVO CRIADO COM SUCESSO**\n\n" +
                "**Arquivo:** %s\n" +
                "**Repositório:** %s\n" +
                "**Mensagem:** %s",
                filePath, repositoryName, message
            );

        } catch (IOException e) {
            log.error("Erro ao criar arquivo", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Atualiza conteúdo de um arquivo
     */
    @Tool("Atualiza o conteúdo de um arquivo existente no repositório")
    public String updateFile(String repositoryName, String filePath, String newContent, String message) {
        try {
            log.info("Atualizando arquivo: {} em {}", filePath, repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            GHContent content = repo.getFileContent(filePath);
            
            String encodedContent = Base64.getEncoder().encodeToString(newContent.getBytes());
            
            content.update(encodedContent, message);

            log.info("✅ Arquivo atualizado com sucesso");
            return String.format(
                "✅ **ARQUIVO ATUALIZADO COM SUCESSO**\n\n" +
                "**Arquivo:** %s\n" +
                "**Repositório:** %s\n" +
                "**Mensagem:** %s",
                filePath, repositoryName, message
            );

        } catch (IOException e) {
            log.error("Erro ao atualizar arquivo", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Deleta um arquivo do repositório
     */
    @Tool("Deleta um arquivo do repositório")
    public String deleteFile(String repositoryName, String filePath, String message) {
        try {
            log.info("Deletando arquivo: {} de {}", filePath, repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            GHContent content = repo.getFileContent(filePath);
            
            content.delete(message);

            log.info("✅ Arquivo deletado com sucesso");
            return String.format(
                "✅ **ARQUIVO DELETADO COM SUCESSO**\n\n" +
                "**Arquivo:** %s\n" +
                "**Repositório:** %s",
                filePath, repositoryName
            );

        } catch (IOException e) {
            log.error("Erro ao deletar arquivo", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Gera um README.md automaticamente
     */
    @Tool("Gera um README.md automaticamente para um repositório")
    public String generateReadme(String repositoryName) {
        try {
            log.info("Gerando README para: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

            String description = repo.getDescription() != null ? repo.getDescription() : "Descrição não disponível";
            String language = repo.getLanguage() != null ? repo.getLanguage() : "N/A";
            String htmlUrl = repo.getHtmlUrl().toString();

            // Gerar README
            String readme = String.format(
                "# %s\n\n" +
                "## 📋 Descrição\n\n" +
                "%s\n\n" +
                "## 💻 Tecnologia\n\n" +
                "- **Linguagem:** %s\n\n" +
                "## 🚀 Como Usar\n\n" +
                "1. Clone o repositório\n" +
                "```bash\n" +
                "git clone %s.git\n" +
                "cd %s\n" +
                "```\n\n" +
                "2. Instale as dependências\n" +
                "```bash\n" +
                "# Use o comando apropriado para sua linguagem\n" +
                "```\n\n" +
                "3. Execute o projeto\n" +
                "```bash\n" +
                "# Execute o projeto\n" +
                "```\n\n" +
                "## 📝 Licença\n\n" +
                "Este projeto está sob a licença MIT.\n\n" +
                "## 👤 Autor\n\n" +
                "[%s](https://github.com/%s)\n",
                repositoryName, description, language, htmlUrl, repositoryName, 
                githubUsername, githubUsername
            );

            return String.format(
                "✅ **README.md GERADO**\n\n" +
                "```markdown\n%s\n```\n\n" +
                "**Próximo passo:** Use createFile() para salvar este README no repositório",
                readme
            );

        } catch (IOException e) {
            log.error("Erro ao gerar README", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista issues de um repositório
     */
    @Tool("Lista todas as issues abertas de um repositório")
    public String listIssues(String repositoryName) {
        try {
            log.info("Listando issues de: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🐛 **ISSUES ABERTAS DE: %s**\n\n", repositoryName));
            
            int count = 0;
            for (org.kohsuke.github.GHIssue issue : repo.getIssues(GHIssueState.OPEN)) {
                sb.append(String.format("#%d - %s\n", issue.getNumber(), issue.getTitle()));
                count++;
                if (count >= 20) break;
            }

            if (count == 0) {
                return "✅ Nenhuma issue aberta.";
            }

            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar issues", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista pull requests
     */
    @Tool("Lista todos os pull requests abertos de um repositório")
    public String listPullRequests(String repositoryName) {
        try {
            log.info("Listando PRs de: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            var prs = repo.getPullRequests(GHIssueState.OPEN);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔄 **PULL REQUESTS ABERTOS DE: %s**\n\n", repositoryName));
            
            int count = 0;
            for (var pr : prs) {
                if (count >= 20) break;
                sb.append(String.format("#%d - %s (por @%s)\n", pr.getNumber(), pr.getTitle(), pr.getUser().getLogin()));
                count++;
            }

            if (count == 0) {
                return "✅ Nenhum PR aberto.";
            }

            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar PRs", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista commits recentes
     */
    @Tool("Lista os commits recentes de um repositório")
    public String listCommits(String repositoryName, int maxResults) {
        try {
            log.info("Listando commits de: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            var commits = repo.listCommits().toList();

            if (commits.isEmpty()) {
                return "📭 Nenhum commit encontrado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📝 **COMMITS RECENTES DE: %s**\n\n", repositoryName));
            
            for (int i = 0; i < Math.min(commits.size(), maxResults); i++) {
                var commit = commits.get(i);
                String sha = commit.getSHA1().substring(0, 7);
                String message = commit.getCommitShortInfo().getMessage().split("\n")[0];
                String author = commit.getCommitShortInfo().getAuthor().getName();
                
                sb.append(String.format("%s - %s (por %s)\n", sha, message, author));
            }

            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar commits", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista branches
     */
    @Tool("Lista todos os branches de um repositório")
    public String listBranches(String repositoryName) {
        try {
            log.info("Listando branches de: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
            var branches = repo.getBranches().values();

            if (branches.isEmpty()) {
                return "📭 Nenhum branch encontrado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🌳 **BRANCHES DE: %s**\n\n", repositoryName));
            
            for (var branch : branches) {
                String name = branch.getName();
                sb.append(String.format("• %s\n", name));
            }

            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao listar branches", e);
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Obtém estatísticas de linguagem
     */
    @Tool("Obtém estatísticas de linguagens de um repositório")
    public String getRepositoryStats(String repositoryName) {
        try {
            log.info("Obtendo stats de: {}", repositoryName);
            
            GitHub gh = getGitHub();
            GHRepository repo = gh.getUser(githubUsername).getRepository(repositoryName);
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

            return sb.toString();

        } catch (IOException e) {
            log.error("Erro ao obter stats", e);
            return "❌ Erro: " + e.getMessage();
        }
    }
}