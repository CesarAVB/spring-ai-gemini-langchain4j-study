package br.com.sistema.springaigemini.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.springaigemini.core.AssistantRegistry;
import br.com.sistema.springaigemini.core.GenericAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assistentes")
@RequiredArgsConstructor
@Tag(name = "Assistentes IA", description = "Endpoints genéricos para interagir com assistentes de IA")
public class GenericAssistantController {

    private final AssistantRegistry assistantRegistry;

    @PostMapping("/{assistantName}/chat")
    @Operation(
            summary = "Enviar mensagem para assistente",
            description = "Processa uma mensagem através do assistente especificado"
    )
    public ResponseEntity<?> sendMessage(
            @PathVariable String assistantName,
            @Valid @RequestBody ChatMessageRequest request) {

        var assistant = assistantRegistry.getAssistant(assistantName);

        if (assistant.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Assistente '" + assistantName + "' não encontrado.",
                            "Assistentes disponíveis: " + String.join(", ",
                                    assistantRegistry.listAssistants())
                    ));
        }

        try {
            String response = assistant.get().processMessage(request.getMessage());
            return ResponseEntity.ok(new ChatMessageResponse(
                    assistantName,
                    request.getMessage(),
                    response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Erro ao processar mensagem",
                            e.getMessage()
                    ));
        }
    }

    @GetMapping
    @Operation(
            summary = "Listar assistentes disponíveis",
            description = "Retorna lista de todos os assistentes registrados no sistema"
    )
    public ResponseEntity<?> listAssistants() {
        List<String> assistants = assistantRegistry.listAssistants();

        if (assistants.isEmpty()) {
            return ResponseEntity.ok(new Object() {
                public String mensagem = "Nenhum assistente registrado";
                public int total = 0;
            });
        }

        return ResponseEntity.ok(new Object() {
            public List<String> assistentes = assistants;
            public int total = assistants.size();
        });
    }

    @GetMapping("/{assistantName}")
    @Operation(
            summary = "Obter informações do assistente",
            description = "Retorna detalhes sobre um assistente específico"
    )
    public ResponseEntity<?> getAssistantInfo(@PathVariable String assistantName) {
        var assistant = assistantRegistry.getAssistant(assistantName);

        if (assistant.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Assistente não encontrado",
                            "Use GET /api/v1/assistentes para listar disponíveis"
                    ));
        }

        GenericAssistant a = assistant.get();
        return ResponseEntity.ok(new Object() {
            public String nome = a.getAssistantName();
            public String descricao = a.getDescription();
        });
    }

    // ==================== DTOs ====================

    public static class ChatMessageRequest {
        public String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatMessageResponse {
        public String assistente;
        public String pergunta;
        public String resposta;

        public ChatMessageResponse(String assistente, String pergunta, String resposta) {
            this.assistente = assistente;
            this.pergunta = pergunta;
            this.resposta = resposta;
        }

        public String getAssistente() {
            return assistente;
        }

        public String getPergunta() {
            return pergunta;
        }

        public String getResposta() {
            return resposta;
        }
    }

    public static class ErrorResponse {
        public String erro;
        public String detalhes;

        public ErrorResponse(String erro, String detalhes) {
            this.erro = erro;
            this.detalhes = detalhes;
        }

        public String getErro() {
            return erro;
        }

        public String getDetalhes() {
            return detalhes;
        }
    }
}