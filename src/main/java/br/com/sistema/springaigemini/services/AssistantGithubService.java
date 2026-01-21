package br.com.sistema.springaigemini.services;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.core.BaseAssistantService;
import br.com.sistema.springaigemini.tools.GithubAssistantTools;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Assistente especializado em gerenciar repositórios GitHub.
 * 
 * FUNCIONALIDADES:
 * ================
 * - Listar repositórios
 * - Listar arquivos de um repositório
 * - Ler conteúdo de arquivos
 * - Criar novos arquivos
 * - Atualizar arquivos existentes
 * - Deletar arquivos
 * - Listar commits
 * - Listar issues abertas
 * - Listar pull requests
 * - Obter informações do repositório
 * - Buscar repositório por nome
 * - Verificar linguagens usadas
 * 
 * INTEGRAÇÃO:
 * ===========
 * - Usa GitHub REST API v3
 * - Integrado com LangChain4j
 * - Usa Google Gemini para entender comandos
 * - Chamada automática de tools
 * 
 * COMO USAR:
 * ==========
 * POST /api/v1/assistentes/AssistenteGithub/chat
 * {
 *   "message": "Liste meus repositórios"
 * }
 * 
 * EXEMPLOS:
 * =========
 * "Liste meus repositórios"
 * "Quais arquivos tem no repo 'meu-projeto'?"
 * "Leia o arquivo README.md do repo 'api-rest'"
 * "Crie um arquivo CONTRIBUINDO.md no repo 'spring-boot-app'"
 * "Mostre os últimos 10 commits de 'java-project'"
 * "Quais issues estão abertas em 'frontend'?"
 * "Liste informações do repositório 'api-gateway'"
 * "Busque um repositório com 'angular' no nome"
 * "Quais linguagens são usadas em 'meu-projeto'?"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantGithubService extends BaseAssistantService {

    private final GithubAssistantTools githubTools;
    private final GoogleAiGeminiChatModel geminiModel;
    private final GithubAiServiceInterface githubAiService;

    @Override
    public String processMessage(String userMessage) {
        try {
            log.info("========================================");
            log.info("Processando mensagem para Github Assistant");
            log.info("Mensagem: {}", userMessage.substring(0, Math.min(80, userMessage.length())));
            log.info("========================================");

            String response = githubAiService.processUserMessage(userMessage);

            logInteraction(getAssistantName(), userMessage, response);
            
            log.info("✅ Processamento concluído com sucesso");
            return response;

        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem no Github Assistant", e);
            return "Erro ao processar requisição do Github: " + e.getMessage();
        }
    }

    @Override
    public String getAssistantName() {
        return "AssistenteGithub";
    }

    @Override
    public String getDescription() {
        return "Assistente inteligente para gerenciar repositórios GitHub usando linguagem natural";
    }

    /**
     * Interface AiService do LangChain4j para GitHub.
     * 
     * Funciona como:
     * 1. Recebe a mensagem do usuário
     * 2. Envia para o Google Gemini com as tools disponíveis
     * 3. Gemini analisa a intenção
     * 4. Seleciona e executa a tool apropriada
     * 5. Retorna resultado formatado
     * 
     * TOOLS DISPONÍVEIS:
     * ==================
     * - listRepositories() - Listar repos
     * - listRepositoryFiles(repoName) - Listar arquivos
     * - readFile(repoName, filePath) - Ler arquivo
     * - createFile(repoName, filePath, content, message) - Criar arquivo
     * - updateFile(repoName, filePath, newContent, message) - Atualizar arquivo
     * - deleteFile(repoName, filePath, message) - Deletar arquivo
     * - listCommits(repoName, maxResults) - Listar commits
     * - listIssues(repoName) - Listar issues
     * - listPullRequests(repoName) - Listar PRs
     * - getRepositoryInfo(repoName) - Informações do repo
     * - searchRepository(searchTerm) - Buscar repo
     * - getRepositoryLanguages(repoName) - Linguagens usadas
     */
    @AiService
    public interface GithubAiServiceInterface {

        @SystemMessage("""
                Você é um assistente inteligente de GitHub baseado em IA.
                
                ========== CONTEXTO ==========
                - Você tem acesso às ferramentas de GitHub
                - Pode listar, criar, ler e atualizar arquivos
                - Pode gerenciar repositórios
                - Seu objetivo é ajudar o usuário a gerenciar GitHub de forma eficiente
                
                ========== TOOLS DISPONÍVEIS ==========
                
                1. listRepositories()
                   - Uso: Quando o usuário quer ver seus repositórios
                   - Exemplo: "Liste meus repositórios"
                   - Retorna: Lista com nome, descrição, linguagem, stars
                
                2. listRepositoryFiles(repoName)
                   - Uso: Quando quer ver arquivos de um repo
                   - Exemplo: "Quais arquivos tem no repo 'meu-projeto'?"
                   - Retorna: Lista de arquivos e pastas
                
                3. readFile(repoName, filePath)
                   - Uso: Quando quer ler conteúdo de um arquivo
                   - Exemplo: "Leia o arquivo README.md de 'api-rest'"
                   - Retorna: Conteúdo completo do arquivo
                
                4. createFile(repoName, filePath, content, message)
                   - Uso: Quando quer criar um novo arquivo
                   - Exemplo: "Crie um arquivo CONTRIBUINDO.md em 'projeto'"
                   - Retorna: Confirmação de criação com URL
                
                5. updateFile(repoName, filePath, newContent, message)
                   - Uso: Quando quer atualizar arquivo existente
                   - Exemplo: "Atualize o README.md do 'projeto'"
                   - Retorna: Confirmação de atualização
                
                6. deleteFile(repoName, filePath, message)
                   - Uso: Quando quer deletar um arquivo
                   - Exemplo: "Delete o arquivo 'old.txt' de 'projeto'"
                   - Retorna: Confirmação de deleção
                
                7. listCommits(repoName, maxResults)
                   - Uso: Quando quer ver histórico de commits
                   - Exemplo: "Mostre os últimos 10 commits de 'projeto'"
                   - Retorna: Lista de commits com autor, data, mensagem
                
                8. listIssues(repoName)
                   - Uso: Quando quer ver issues abertas
                   - Exemplo: "Quais issues estão abertas em 'api'?"
                   - Retorna: Lista de issues abertas
                
                9. listPullRequests(repoName)
                   - Uso: Quando quer ver PRs abertos
                   - Exemplo: "Liste os PRs abertos de 'frontend'"
                   - Retorna: Lista de pull requests
                
                10. getRepositoryInfo(repoName)
                    - Uso: Quando quer informações detalhadas
                    - Exemplo: "Mostre informações de 'projeto'"
                    - Retorna: Stars, forks, linguagem, datas, etc
                
                11. searchRepository(searchTerm)
                    - Uso: Quando quer buscar um repo por nome
                    - Exemplo: "Busque um repo com 'angular'"
                    - Retorna: Resultados da busca
                
                12. getRepositoryLanguages(repoName)
                    - Uso: Quando quer ver linguagens usadas
                    - Exemplo: "Quais linguagens tem em 'projeto'?"
                    - Retorna: Linguagens com percentual
                
                ========== REGRAS IMPORTANTES ==========
                
                ✓ LISTAR REPOSITÓRIOS:
                  - Sempre mostre nome, descrição e stars
                  - Organize por ordem de atualização
                
                ✓ LER ARQUIVOS:
                  - Confirme o repositório e caminho
                  - Mostre o conteúdo formatado
                
                ✓ CRIAR/ATUALIZAR ARQUIVOS:
                  - Sempre peça confirmação
                  - Inclua mensagem de commit descritiva
                
                ✓ COM LINGUAGEM NATURAL:
                  - Se não souber exatamente qual tool usar, pergunte
                  - Sempre confirme a intenção do usuário
                
                ✓ NA RESPOSTA:
                  - Seja claro e conciso
                  - Use formatação Markdown
                  - Inclua links quando disponível
                
                ========== EXEMPLOS DE INTERAÇÃO ==========
                
                EXEMPLO 1 - Listar Repos:
                Usuário: "Quais são meus repositórios?"
                IA: Chama listRepositories() e exibe resultado
                
                EXEMPLO 2 - Ler Arquivo:
                Usuário: "Leia o arquivo package.json do repo 'web-app'"
                IA: Chama readFile("web-app", "package.json")
                
                EXEMPLO 3 - Criar Arquivo:
                Usuário: "Crie um arquivo README.md em 'novo-projeto'"
                IA: Pede o conteúdo e depois chama createFile()
                
                EXEMPLO 4 - Buscar Repo:
                Usuário: "Tenho algum repo com 'api' no nome?"
                IA: Chama searchRepository("api")
                
                ========== DICAS IMPORTANTES ==========
                
                • Sempre confirme a ação antes de criar/atualizar/deletar
                • Se o repositório não existir, ofereça ajuda
                • Mostre URLs dos arquivos quando possível
                • Explique o que cada comando faz
                • Ofereça sugestões de próximas ações
                
                Você está pronto para ajudar com GitHub! 🚀
                """)
        String processUserMessage(@UserMessage String userMessage);
    }
}