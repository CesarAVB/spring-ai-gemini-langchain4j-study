package br.com.sistema.springaigemini.services;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.core.BaseAssistantService;
import br.com.sistema.springaigemini.dtos.CalculoPlanoCompleteRequest;
import br.com.sistema.springaigemini.dtos.PlanoNutricionalDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import lombok.RequiredArgsConstructor;

/**
 * Assistente especializado em gerar e personalizar planos nutricionais.
 * 
 * IMPORTANTE: Este assistente é totalmente independente.
 * - Não depende de nenhuma entidade do projeto de nutrição
 * - Recebe dados via DTOs (vindo de outro microserviço)
 * - Realiza cálculos internamente
 * - Retorna resultado via PlanoNutricionalDTO
 */
@Service
@RequiredArgsConstructor
public class AssistantPlanoService extends BaseAssistantService {

    private final PlanoNutricionalCalculatorService calculatorService;

    /**
     * Calcula um plano nutricional completo.
     * 
     * @param request contém PacienteDTO, AvaliacaoFisicaDTO, objetivo, intensidade
     * @return plano calculado
     */
    public PlanoNutricionalDTO calcularPlano(CalculoPlanoCompleteRequest request) {
        return calculatorService.calcularPlano(
                request.paciente(),
                request.avaliacaoFisica(),
                request.objetivo(),
                request.intensidadeExercicio()
        );
    }

    /**
     * Processa mensagem de usuário (para integração com LangChain4j).
     * 
     * @param userMessage mensagem do usuário
     * @return resposta processada
     */
    @Override
    public String processMessage(String userMessage) {
        // Implementação será feita com @AiService do LangChain4j
        throw new UnsupportedOperationException(
                "Integração com LangChain4j @AiService necessária"
        );
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
     * System message que define o contexto para geração de planos.
     * Use esta anotação ao criar a interface AiService.
     */
    @SystemMessage("""
            Você é um assistente especializado em geração e personalização de planos nutricionais.
            
            CONTEXTO:
            - Atua em um microserviço independente de gestão nutricional
            - Recebe dados do paciente via API (não consulta banco local)
            - Gera planos baseados em dados reais (altura, peso, idade, objetivo)
            - Tem acesso a ferramentas de cálculo nutricional
            
            DETECÇÃO DE INTENÇÃO:
            
            USE calculateNutritionalPlan() se:
            - "Gere um plano para [paciente/ID]"
            - "Qual é o plano nutricional para..."
            - "Calcula quantas calorias..."
            - "Qual deve ser a ingestão de proteína..."
            
            NÃO USE se:
            - Pergunta é apenas informativa (ex: "o que é proteína?")
            - Faltam dados para cálculo
            - Usuário não especificou paciente
            
            REGRAS:
            1. Sempre confirme dados do paciente
            2. Peça objetivo e intensidade se não fornecidos
            3. Explique resultado em linguagem clara
            4. Destaque: TMB, calorias alvo, macros
            5. Sempre dê recomendações personalizadas
            """)
    public String generatePlan(@UserMessage String userMessage) {
        return processMessage(userMessage);
    }

    /**
     * System message para ajuste de planos já calculados.
     */
    @SystemMessage("""
            Você recebe um plano nutricional já calculado e pode ajustá-lo conforme necessário.
            
            CAPACIDADES:
            - Explicar cada componente do plano
            - Sugerir ajustes (aumentar/diminuir calorias, rebalancear macros)
            - Responder dúvidas sobre o plano
            - Criar variações (ex: versão com mais proteína, menos carbs)
            - Oferecer alternativas alimentares
            """)
    public String adjustPlan(@UserMessage String userMessage) {
        return processMessage(userMessage);
    }
}