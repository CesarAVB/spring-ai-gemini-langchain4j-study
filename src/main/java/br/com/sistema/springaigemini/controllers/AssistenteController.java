package br.com.sistema.springaigemini.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.springaigemini.dtos.request.plano.CreatePlanoRequest;
import br.com.sistema.springaigemini.dtos.response.common.RespostaAssistente;
import br.com.sistema.springaigemini.dtos.response.plano.PlanoResponse;
import br.com.sistema.springaigemini.enums.TipoResposta;
import br.com.sistema.springaigemini.services.AssistantGithubService;
import br.com.sistema.springaigemini.services.AssistantGmailService;
import br.com.sistema.springaigemini.services.AssistantPlanoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Controller para todos os assistentes.
 * Retorna respostas estruturadas como Records
 * 
 * Assistentes disponíveis:
 * - AssistenteGmail: Gerenciar emails Gmail
 * - AssistenteGithub: Gerenciar repositórios GitHub
 * - AssistentePlano: Cálculo de plano nutricional
 */
@RestController
@RequestMapping("/api/v1/assistentes")
@RequiredArgsConstructor
@Log4j2
@Tag(
    name = "Assistentes IA",
    description = "API para interagir com assistentes de IA (Gmail, GitHub, Plano Nutricional)"
)
public class AssistenteController {

    private final AssistantGmailService gmailService;
    private final AssistantGithubService githubService;
    private final AssistantPlanoService planoService;

    /**
     * GET /api/v1/assistentes - Lista assistentes disponíveis
     */
    @GetMapping
    @Operation(
        summary = "Listar assistentes disponíveis",
        description = "Retorna uma lista de todos os assistentes IA disponíveis no sistema"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de assistentes obtida com sucesso",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                  "AssistenteGmail": "Gerenciar emails Gmail",
                  "AssistenteGithub": "Gerenciar repositórios GitHub",
                  "AssistentePlano": "Cálculo de plano nutricional"
                }
                """
            )
        )
    )
    public ResponseEntity<Map<String, Object>> listarAssistentes() {
        log.info("Listando assistentes");
        Map<String, Object> assistentes = new HashMap<>();
        assistentes.put("AssistenteGmail", "Gerenciar emails Gmail");
        assistentes.put("AssistenteGithub", "Gerenciar repositórios GitHub");
        assistentes.put("AssistentePlano", "Cálculo de plano nutricional");
        return ResponseEntity.ok(assistentes);
    }

    /**
     * GET /api/v1/assistentes/{nome} - Info de um assistente
     */
    @GetMapping("/{nome}")
    @Operation(
        summary = "Obter informações de um assistente específico",
        description = "Retorna detalhes e operações disponíveis para um assistente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Informações do assistente obtidas com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "nome": "AssistenteGmail",
                      "descricao": "Gerenciar emails Gmail",
                      "operacoes": ["Listar", "Enviar", "Deletar", "Buscar"]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assistente não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "sucesso": false,
                      "assistente": "AssistenteInexistente",
                      "tipo": "erro",
                      "erro": {
                        "codigo": "NAO_ENCONTRADO",
                        "mensagem": "Assistente não existe",
                        "statusHttp": 404
                      },
                      "timestamp": "2025-01-20T20:42:19"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> infoAssistente(
            @Parameter(
                name = "nome",
                description = "Nome do assistente (AssistenteGmail, AssistenteGithub, AssistentePlano)",
                example = "AssistenteGmail"
            )
            @PathVariable String nome) {
        
        log.info("Info assistente: {}", nome);
        Map<String, Object> info = new HashMap<>();
        
        return switch (nome.toLowerCase()) {
            case "assistentegmail" -> {
                info.put("nome", "AssistenteGmail");
                info.put("descricao", "Gerenciar emails Gmail");
                info.put("operacoes", new String[]{"Listar", "Enviar", "Deletar", "Buscar"});
                yield ResponseEntity.ok(info);
            }
            case "assistentegithub" -> {
                info.put("nome", "AssistenteGithub");
                info.put("descricao", "Gerenciar repositórios GitHub");
                info.put("operacoes", new String[]{"Listar repos", "Ler arquivos", "Criar arquivos"});
                yield ResponseEntity.ok(info);
            }
            case "assistenteplano" -> {
                info.put("nome", "AssistentePlano");
                info.put("descricao", "Cálculo de plano nutricional");
                info.put("operacoes", new String[]{"Calcular plano", "Validar dados", "Formatar resultado"});
                yield ResponseEntity.ok(info);
            }
            default -> ResponseEntity.status(404)
                .body(RespostaAssistente.erro(nome, "NAO_ENCONTRADO", "Assistente não existe", 404));
        };
    }

    /**
     * POST /api/v1/assistentes/{nome}/chat - Enviar pergunta
     */
    @PostMapping("/{nome}/chat")
    @Operation(
        summary = "Enviar pergunta para um assistente",
        description = """
            Envia uma pergunta/comando para um assistente específico.
            
            Exemplos de uso:
            - Gmail: "Liste meus últimos 10 emails", "Envie um email para..."
            - GitHub: "Liste meus repositórios", "Leia o arquivo README.md do..."
            - Plano: "Calcule meu plano nutricional"
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pergunta processada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RespostaAssistente.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "sucesso": true,
                      "assistente": "AssistenteGmail",
                      "tipo": "texto",
                      "pergunta": "Liste meus últimos 10 emails",
                      "dados": "Aqui estão seus 10 últimos e-mails...",
                      "erro": null,
                      "timestamp": "2025-01-20T20:42:19"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Mensagem vazia ou inválida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "sucesso": false,
                      "assistente": "AssistenteGmail",
                      "tipo": "erro",
                      "erro": {
                        "codigo": "MENSAGEM_VAZIA",
                        "mensagem": "Mensagem não pode estar vazia",
                        "statusHttp": 400
                      },
                      "timestamp": "2025-01-20T20:42:19"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assistente não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "sucesso": false,
                      "assistente": "AssistenteInexistente",
                      "tipo": "erro",
                      "erro": {
                        "codigo": "ASSISTENTE_NAO_ENCONTRADO",
                        "mensagem": "Assistente não existe",
                        "statusHttp": 404
                      },
                      "timestamp": "2025-01-20T20:42:19"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao processar a pergunta",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "sucesso": false,
                      "assistente": "AssistenteGmail",
                      "tipo": "erro",
                      "erro": {
                        "codigo": "ERRO_PROCESSAMENTO",
                        "mensagem": "Erro ao processar a pergunta",
                        "detalhe": "Connection refused",
                        "statusHttp": 500
                      },
                      "timestamp": "2025-01-20T20:42:19"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<RespostaAssistente> chat(
            @Parameter(
                name = "nome",
                description = "Nome do assistente",
                example = "AssistenteGmail"
            )
            @PathVariable String nome,
            
            @RequestBody
            @Parameter(
                description = "Pergunta ou comando para o assistente",
                example = """
                {
                  "message": "Liste meus últimos 10 emails"
                }
                """
            )
            Map<String, String> request) {
        
        log.info("Chat com assistente: {} - Pergunta: {}", nome, request.get("message"));
        
        try {
            String mensagem = request.get("message");
            
            if (mensagem == null || mensagem.trim().isEmpty()) {
                var erro = RespostaAssistente.erro(
                    nome, "MENSAGEM_VAZIA", "Mensagem não pode estar vazia", 400
                );
                return ResponseEntity.badRequest().body(erro);
            }

            var resposta = switch (nome.toLowerCase()) {
                case "assistentegmail" -> {
                    String respostaGmail = gmailService.processMessage(mensagem);
                    yield RespostaAssistente.sucesso(
                        nome, 
                        TipoResposta.TEXTO.getValor(),  // ✅ CORRIGIDO
                        mensagem, 
                        respostaGmail
                    );
                }
                case "assistentegithub" -> {
                    String respostaGithub = githubService.processMessage(mensagem);
                    yield RespostaAssistente.sucesso(
                        nome, 
                        TipoResposta.TEXTO.getValor(),  // ✅ CORRIGIDO
                        mensagem, 
                        respostaGithub
                    );
                }
                case "assistenteplano" -> {
                    String respostaPlano = planoService.processMessage(mensagem);
                    yield RespostaAssistente.sucesso(
                        nome, 
                        TipoResposta.TEXTO.getValor(),  // ✅ CORRIGIDO
                        mensagem, 
                        respostaPlano
                    );
                }
                default -> RespostaAssistente.erro(
                    nome, 
                    "ASSISTENTE_NAO_ENCONTRADO", 
                    "Assistente não existe", 
                    404
                );
            };

            log.info("✅ Resposta processada para: {}", nome);
            return resposta.sucesso() ? ResponseEntity.ok(resposta) : ResponseEntity.status(404).body(resposta);

        } catch (Exception e) {
            log.error("❌ Erro ao processar pergunta", e);
            var erro = RespostaAssistente.erro(
                nome, 
                "ERRO_PROCESSAMENTO", 
                e.getMessage(), 
                500
            );
            return ResponseEntity.status(500).body(erro);
        }
    }

    /**
     * POST /api/v1/assistentes/plano/calcular - Calcular plano nutricional
     * 
     * Endpoint específico para calcular plano nutricional com request estruturado
     */
    @PostMapping("/plano/calcular")
    @Operation(
        summary = "Calcular plano nutricional",
        description = "Calcula um plano nutricional personalizado baseado nos dados fornecidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plano calculado com sucesso",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro ao calcular"
        )
    })
    public ResponseEntity<PlanoResponse> calcularPlano(
            @RequestBody CreatePlanoRequest request) {
        
        log.info("Calculando plano para: {}", request.nome());
        
        try {
            if (!planoService.validarRequest(request)) {
                return ResponseEntity.badRequest().build();
            }
            
            PlanoResponse response = planoService.calcularPlano(request);
            log.info("✅ Plano calculado com sucesso");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Erro ao calcular plano", e);
            return ResponseEntity.status(500).build();
        }
    }
}