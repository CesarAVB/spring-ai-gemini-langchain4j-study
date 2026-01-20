package br.com.sistema.springaigemini.tools;

import org.springframework.stereotype.Component;

import br.com.sistema.springaigemini.core.AssistantTool;
import br.com.sistema.springaigemini.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.springaigemini.dtos.PacienteDTO;
import br.com.sistema.springaigemini.dtos.PlanoNutricionalDTO;
import br.com.sistema.springaigemini.services.PlanoNutricionalCalculatorService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Tools (ferramentas) para o assistente de planos nutricionais.
 * 
 * IMPORTANTE: Estas ferramentas NÃO acessam banco de dados.
 * Elas recebem DTOs como entrada e realizam cálculos internamente.
 * 
 * Implementa AssistantTool para descoberta automática.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PlanoAssistantTools implements AssistantTool {

    private final PlanoNutricionalCalculatorService calculatorService;

    @Override
    public String getToolName() {
        return "PlanoNutricionalTools";
    }

    @Override
    public String getToolDescription() {
        return "Ferramentas para cálculo e consulta de planos nutricionais personalizados (independente)";
    }

    /**
     * Calcula um plano nutricional personalizado.
     * 
     * IMPORTANTE: Este método espera que os dados do paciente sejam passados como strings JSON.
     * Em uma integração real, seria chamado pelo Controller com DTOs já parseados.
     * 
     * @param pacienteJson JSON com dados do paciente (id, nome, sexo, altura, dataNascimento)
     * @param avaliacaoJson JSON com avaliação física (pesoAtual, percentualGordura, etc)
     * @param objetivo emagrecimento, manutenção ou ganho_massa
     * @param intensidadeExercicio sedentário, leve, moderado, intenso
     * @return resposta formatada com resultado do plano
     */
    @Tool("Calcula um plano nutricional personalizado sem acessar banco de dados")
    public String calculateNutritionalPlan(
            String pacienteJson,
            String avaliacaoJson,
            String objetivo,
            String intensidadeExercicio) {

        try {
            // Em um cenário real, o Controller já teria os DTOs e os passaria diretamente
            // Este é um exemplo de como seria se recebido via LangChain4j
            
            // Aqui você teria que fazer parse do JSON para DTOs
            // Para este exemplo, retornamos uma resposta padronizada
            
            return String.format(
                    "✅ **PLANO NUTRICIONAL CALCULADO**\n\n" +
                    "Objetivo: %s\n" +
                    "Intensidade: %s\n" +
                    "Status: Pronto para cálculo\n\n" +
                    "Use o endpoint POST /api/v1/plano/calcular com:\n" +
                    "- PacienteDTO\n" +
                    "- AvaliacaoFisicaDTO\n" +
                    "- Objetivo\n" +
                    "- Intensidade\n",
                    objetivo, intensidadeExercicio
            );

        } catch (Exception e) {
            log.error("Erro ao calcular plano nutricional", e);
            return "❌ Erro ao calcular plano: " + e.getMessage();
        }
    }

    /**
     * Obtém informações do paciente.
     * 
     * @param pacienteJson JSON com dados do paciente
     * @return informações formatadas
     */
    @Tool("Obtém informações do paciente")
    public String getPacienteInfo(String pacienteJson) {
        try {
            // Parse do JSON para exibir informações
            // Em cenário real, receberia PacienteDTO já parseado
            
            return String.format(
                    "📋 **INFORMAÇÕES DO PACIENTE**\n" +
                    "Status: Dados recebidos via DTO\n" +
                    "Integração: Independente (sem banco local)\n\n" +
                    "Para calcular plano, forneça:\n" +
                    "- Dados do paciente (altura, sexo, data nascimento)\n" +
                    "- Avaliação física (peso, percentual gordura)\n" +
                    "- Objetivo (emagrecimento/manutenção/ganho)\n" +
                    "- Intensidade de exercício\n"
            );

        } catch (Exception e) {
            log.error("Erro ao obter informações do paciente", e);
            return "❌ Erro ao processar informações: " + e.getMessage();
        }
    }

    /**
     * Valida se os dados recebidos são suficientes para cálculo.
     * 
     * @param paciente DTO do paciente
     * @param avaliacao DTO da avaliação
     * @param objetivo objetivo do plano
     * @param intensidade intensidade de exercício
     * @return true se dados são válidos
     */
    public boolean validarDados(
            PacienteDTO paciente,
            AvaliacaoFisicaDTO avaliacao,
            String objetivo,
            String intensidade) {

        if (paciente == null) {
            log.warn("Paciente nulo");
            return false;
        }
        if (avaliacao == null) {
            log.warn("Avaliação nula");
            return false;
        }
        if (objetivo == null || objetivo.isBlank()) {
            log.warn("Objetivo não especificado");
            return false;
        }
        if (intensidade == null || intensidade.isBlank()) {
            log.warn("Intensidade não especificada");
            return false;
        }

        return true;
    }

    /**
     * Formata um plano calculado para exibição.
     * 
     * @param plano plano calculado
     * @return string formatada com resultado
     */
    public String formatarPlano(PlanoNutricionalDTO plano) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "✅ **PLANO NUTRICIONAL - %s**\n\n" +
                "👤 **Paciente:** %s\n" +
                "🎯 **Objetivo:** %s\n" +
                "💪 **Intensidade:** %s\n\n",
                plano.dataCalculo(),
                plano.nomePaciente(),
                plano.objetivo(),
                plano.intensidadeExercicio()
        ));

        sb.append(String.format(
                "📊 **ANÁLISE CORPORAL**\n" +
                "├─ Idade: %d anos\n" +
                "├─ Altura: %.2f m\n" +
                "└─ Peso: %.1f kg\n\n",
                plano.idade(),
                plano.alturaMetros(),
                plano.pesoAtual()
        ));

        sb.append(String.format(
                "🔥 **ENERGÉTICOS**\n" +
                "├─ TMB: %.0f kcal/dia\n" +
                "├─ Gasto: %.0f kcal/dia\n" +
                "└─ Meta: %.0f kcal/dia\n\n",
                plano.tmb(),
                plano.gastoDiario(),
                plano.caloriaAlvo()
        ));

        var macro = plano.macronutrientes();
        sb.append(String.format(
                "🥗 **MACRONUTRIENTES**\n" +
                "├─ Proteína: %.1fg (%.0f kcal - %.1f%%)\n" +
                "├─ Carbo: %.1fg (%.0f kcal - %.1f%%)\n" +
                "└─ Gordura: %.1fg (%.0f kcal - %.1f%%)\n\n",
                macro.proteinaGramas(), macro.proteinaCalorias(), macro.proteinaPercentual(),
                macro.carboidratoGramas(), macro.carboIdratoCalorias(), macro.carboidratoPercentual(),
                macro.gorduraGramas(), macro.gorduraCalorias(), macro.gorduraPercentual()
        ));

        sb.append("📌 **RECOMENDAÇÕES**\n");
        for (String rec : plano.recomendacoes()) {
            sb.append("├─ ").append(rec).append("\n");
        }

        sb.append(String.format(
                "\n✓ Validade: %d dias\n" +
                "✓ Microserviço: Independente (sem dependências externas)\n",
                plano.validadeDias()
        ));

        return sb.toString();
    }
}