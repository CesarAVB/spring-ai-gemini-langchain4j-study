package br.com.sistema.springaigemini.services;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.core.BaseAssistantService;
import br.com.sistema.springaigemini.dtos.request.plano.CreatePlanoRequest;
import br.com.sistema.springaigemini.dtos.response.plano.PlanoResponse;
import br.com.sistema.springaigemini.mappers.response.plano.PlanoResponseMapper;
import br.com.sistema.springaigemini.models.PlanoNutricional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Assistente especializado em gerar e personalizar planos nutricionais.
 * 
 * IMPORTANTE: Este assistente é totalmente independente.
 * - Não depende de nenhuma entidade externa
 * - Recebe CreatePlanoRequest como entrada
 * - Realiza cálculos internamente
 * - Retorna PlanoResponse (via mapper)
 * 
 * Flow: CreatePlanoRequest → PlanoNutricional → PlanoResponse
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AssistantPlanoService extends BaseAssistantService {

    private final PlanoNutricionalCalculatorService calculatorService;
    private final PlanoResponseMapper planoResponseMapper;

    /**
     * Calcula um plano nutricional completo.
     * 
     * @param request contém nome, idade, pesoAtual, objetivo, intensidadeExercicio, recomendacoes
     * @return PlanoResponse com resultado do cálculo
     * @throws IllegalArgumentException se dados forem inválidos
     */
    public PlanoResponse calcularPlano(CreatePlanoRequest request) {
        log.info("Iniciando cálculo de plano para: {}", request.nome());
        
        try {
            // 1. Calcular plano (CreatePlanoRequest → PlanoNutricional)
            PlanoNutricional plano = calculatorService.calcularPlano(request);
            
            // 2. Converter para Response (PlanoNutricional → PlanoResponse)
            PlanoResponse response = planoResponseMapper.toPlanoResponse(plano);
            
            log.info("✅ Plano calculado com sucesso");
            return response;
            
        } catch (Exception e) {
            log.error("❌ Erro ao calcular plano", e);
            throw e;
        }
    }

    /**
     * Processa mensagem de usuário (integração com LangChain4j).
     * 
     * @param userMessage mensagem do usuário
     * @return resposta processada
     */
    @Override
    public String processMessage(String userMessage) {
        // Será implementado com @AiService do LangChain4j quando necessário
        log.warn("processMessage ainda não implementado");
        return "Assistente de planos ainda não integrado com IA";
    }

    @Override
    public String getAssistantName() {
        return "AssistentePlano";
    }

    @Override
    public String getDescription() {
        return "Gera e personaliza planos nutricionais baseado em dados do paciente (independente)";
    }

    /**
     * Valida dados do request antes de calcular.
     * 
     * @param request CreatePlanoRequest a validar
     * @return true se válido
     */
    public boolean validarRequest(CreatePlanoRequest request) {
        if (request == null) {
            log.warn("Request nulo");
            return false;
        }
        if (request.nome() == null || request.nome().isBlank()) {
            log.warn("Nome inválido");
            return false;
        }
        if (request.idade() == null || request.idade() <= 0) {
            log.warn("Idade inválida");
            return false;
        }
        if (request.pesoAtual() == null || request.pesoAtual() <= 0) {
            log.warn("Peso inválido");
            return false;
        }
        if (request.objetivo() == null || request.objetivo().isBlank()) {
            log.warn("Objetivo inválido");
            return false;
        }
        if (request.intensidadeExercicio() == null || request.intensidadeExercicio().isBlank()) {
            log.warn("Intensidade inválida");
            return false;
        }
        return true;
    }

    /**
     * Formata um plano para exibição.
     * 
     * @param response PlanoResponse a formatar
     * @return string formatada
     */
    public String formatarPlanoParaExibicao(PlanoResponse response) {
        if (response == null) {
            return "❌ Plano nulo";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✅ **PLANO NUTRICIONAL**\n\n");
        
        sb.append(String.format(
            "👤 **Paciente:** %s\n" +
            "📊 **Idade:** %d anos\n" +
            "⚖️ **Peso:** %.1f kg\n" +
            "🎯 **Objetivo:** %s\n" +
            "💪 **Intensidade:** %s\n\n",
            response.nome(),
            response.idade(),
            response.pesoAtual(),
            response.objetivo(),
            response.intensidadeExercicio()
        ));

        sb.append("📌 **RECOMENDAÇÕES**\n");
        if (response.recomendacoes() != null && !response.recomendacoes().isEmpty()) {
            for (String rec : response.recomendacoes()) {
                sb.append("├─ ").append(rec).append("\n");
            }
        } else {
            sb.append("├─ Nenhuma recomendação específica\n");
        }

        sb.append("\n✓ Microserviço: Independente (sem dependências externas)\n");

        return sb.toString();
    }
}