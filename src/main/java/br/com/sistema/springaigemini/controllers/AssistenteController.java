package br.com.sistema.springaigemini.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.springaigemini.dtos.CalculoPlanoCompleteRequest;
import br.com.sistema.springaigemini.dtos.PlanoNutricionalDTO;
import br.com.sistema.springaigemini.services.AssistantPlanoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plano")
@RequiredArgsConstructor
@Tag(name = "Plano Nutricional", description = "Endpoints para cálculo de planos nutricionais (microserviço independente)")
public class AssistenteController {

    private final AssistantPlanoService assistantPlanoService;

    /**
     * Calcula um plano nutricional completo.
     * 
     * Este endpoint é independente e recebe todos os dados necessários via DTOs.
     * Não faz chamadas ao banco de dados do projeto de nutrição.
     * 
     * @param request contém PacienteDTO, AvaliacaoFisicaDTO, objetivo, intensidade
     * @return plano nutricional calculado
     */
    @PostMapping("/calcular")
    @Operation(
            summary = "Calcular plano nutricional",
            description = "Calcula um plano nutricional personalizado baseado nos dados fornecidos. " +
                         "Microserviço independente - não requer integração com banco de dados local."
    )
    public ResponseEntity<PlanoNutricionalDTO> calcularPlano(
            @Valid @RequestBody CalculoPlanoCompleteRequest request) {

        try {
            PlanoNutricionalDTO plano = assistantPlanoService.calcularPlano(request);
            return ResponseEntity.ok(plano);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Valida se os dados fornecidos são suficientes para cálculo.
     * 
     * @param request dados para validação
     * @return mensagem de validação
     */
    @PostMapping("/validar")
    @Operation(
            summary = "Validar dados para cálculo",
            description = "Verifica se os dados fornecidos são suficientes e válidos para calcular um plano"
    )
    public ResponseEntity<?> validarDados(
            @Valid @RequestBody CalculoPlanoCompleteRequest request) {

        if (request.paciente() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Object() {
                        public String erro = "Dados do paciente são obrigatórios";
                    });
        }

        if (request.avaliacaoFisica() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Object() {
                        public String erro = "Avaliação física é obrigatória";
                    });
        }

        return ResponseEntity.ok(new Object() {
            public boolean valido = true;
            public String mensagem = "Dados validados com sucesso. Pronto para calcular plano.";
        });
    }

    /**
     * Retorna informações sobre este serviço.
     * 
     * @return informações do serviço
     */
    @GetMapping("/info")
    @Operation(
            summary = "Informações do serviço",
            description = "Retorna informações sobre o microserviço de assistentes"
    )
    public ResponseEntity<?> getInfo() {
        return ResponseEntity.ok(new Object() {
            public String nome = "Microserviço de Assistentes Nutricionais";
            public String descricao = "Serviço independente para cálculo de planos nutricionais";
            public String independencia = "Não depende de nenhuma entidade local. Recebe dados via DTOs.";
            public String versao = "1.0.0";
            public String endpoint = "POST /api/v1/plano/calcular";
        });
    }

    /**
     * Retorna exemplo de requisição.
     * 
     * @return exemplo de CalculoPlanoCompleteRequest
     */
    @GetMapping("/exemplo")
    @Operation(
            summary = "Exemplo de requisição",
            description = "Retorna um exemplo de como fazer uma requisição para calcular plano"
    )
    public ResponseEntity<?> getExemplo() {
        return ResponseEntity.ok(new Object() {
            public String descricao = "Exemplo de requisição POST /api/v1/plano/calcular";
            public Object exemplo = new Object() {
                public Object paciente = new Object() {
                    public Long id = 1L;
                    public String nome = "João Silva";
                    public String sexo = "M";
                    public Double altura = 1.75;
                    public String data_nascimento = "1990-01-15";
                    public String cpf = "123.456.789-00";
                    public String email = "joao@email.com";
                    public String telefone = "11999999999";
                };
                public Object avaliacaoFisica = new Object() {
                    public Long id = 1L;
                    public Double peso_atual = 85.5;
                    public Double percentual_gordura = 18.5;
                    public Double massa_magra = 65.6;
                    public Double massa_gorda = 14.9;
                    public Double imc = 26.3;
                    public String data_avaliacao = "2025-01-20";
                };
                public String objetivo = "emagrecimento";
                public String intensidadeExercicio = "moderado";
                public String observacoes = "opcional";
            };
        });
    }
}