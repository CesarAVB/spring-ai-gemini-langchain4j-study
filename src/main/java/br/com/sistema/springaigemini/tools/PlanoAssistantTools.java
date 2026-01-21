package br.com.sistema.springaigemini.tools;

import org.springframework.stereotype.Component;

import br.com.sistema.springaigemini.core.AssistantTool;
import br.com.sistema.springaigemini.dtos.request.plano.CreatePlanoRequest;
import br.com.sistema.springaigemini.dtos.response.plano.PlanoResponse;
import br.com.sistema.springaigemini.models.PlanoNutricional;
import br.com.sistema.springaigemini.models.PlanoNutricional.Macronutrientes;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Tools (ferramentas) para o assistente de planos nutricionais.
 * 
 * Recebe CreatePlanoRequest e retorna PlanoResponse via mappers.
 * Implementa AssistantTool para descoberta automática.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PlanoAssistantTools implements AssistantTool {

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
     * @param createPlanoRequest Request com dados do plano
     * @return resposta formatada com resultado do plano
     */
    @Tool("Calcula um plano nutricional personalizado sem acessar banco de dados")
    public String calculateNutritionalPlan(CreatePlanoRequest createPlanoRequest) {

        try {
            if (createPlanoRequest == null) {
                return "❌ Request nulo";
            }
            
            if (!validarDados(createPlanoRequest)) {
                return "❌ Dados inválidos para cálculo de plano";
            }
            
            return String.format(
                    "✅ **PLANO NUTRICIONAL CALCULADO**\n\n" +
                    "Paciente: %s\n" +
                    "Idade: %d anos\n" +
                    "Peso: %.1f kg\n" +
                    "Objetivo: %s\n" +
                    "Intensidade: %s\n" +
                    "Recomendações: %d\n" +
                    "Status: Pronto para cálculo\n\n" +
                    "Use o endpoint POST /api/v1/plano/calcular com:\n" +
                    "- CreatePlanoRequest\n",
                    createPlanoRequest.nome(),
                    createPlanoRequest.idade(),
                    createPlanoRequest.pesoAtual(),
                    createPlanoRequest.objetivo(), 
                    createPlanoRequest.intensidadeExercicio(),
                    createPlanoRequest.recomendacoes() != null ? createPlanoRequest.recomendacoes().size() : 0
            );

        } catch (Exception e) {
            log.error("Erro ao calcular plano nutricional", e);
            return "❌ Erro ao calcular plano: " + e.getMessage();
        }
    }

    /**
     * Obtém informações do plano calculado.
     * 
     * @param planoResponse Response com dados do plano
     * @return informações formatadas
     */
    @Tool("Obtém informações do plano nutricional calculado")
    public String getPlanoInfo(PlanoResponse planoResponse) {
        try {
            if (planoResponse == null) {
                return "❌ Plano nulo";
            }
            
            return String.format(
                    "📋 **INFORMAÇÕES DO PLANO**\n\n" +
                    "Nome: %s\n" +
                    "Idade: %d anos\n" +
                    "Peso Atual: %.1f kg\n" +
                    "Objetivo: %s\n" +
                    "Intensidade: %s\n" +
                    "Recomendações: %d\n\n" +
                    "Status: Dados recebidos via PlanoResponse\n" +
                    "Integração: Independente (sem banco local)\n",
                    planoResponse.nome(),
                    planoResponse.idade(),
                    planoResponse.pesoAtual(),
                    planoResponse.objetivo(),
                    planoResponse.intensidadeExercicio(),
                    planoResponse.recomendacoes() != null ? planoResponse.recomendacoes().size() : 0
            );

        } catch (Exception e) {
            log.error("Erro ao obter informações do plano", e);
            return "❌ Erro ao processar informações: " + e.getMessage();
        }
    }

    /**
     * Valida se os dados recebidos são suficientes para cálculo.
     * 
     * @param request Request com dados do plano
     * @return true se dados são válidos
     */
    private boolean validarDados(CreatePlanoRequest request) {

        if (request == null) {
            log.warn("Request nula");
            return false;
        }
        if (request.nome() == null || request.nome().isBlank()) {
            log.warn("Nome não especificado");
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
            log.warn("Objetivo não especificado");
            return false;
        }
        if (request.intensidadeExercicio() == null || request.intensidadeExercicio().isBlank()) {
            log.warn("Intensidade não especificada");
            return false;
        }

        return true;
    }

    /**
     * Formata um plano calculado para exibição.
     * 
     * @param plano PlanoNutricional com dados calculados
     * @return string formatada com resultado
     */
    public String formatarPlano(PlanoNutricional plano) {
        if (plano == null) {
            return "❌ Plano nulo";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "✅ **PLANO NUTRICIONAL**\n\n" +
                "👤 **Paciente:** %s\n" +
                "📊 **Idade:** %d anos\n" +
                "⚖️ **Peso:** %.1f kg\n" +
                "🎯 **Objetivo:** %s\n" +
                "💪 **Intensidade:** %s\n\n",
                plano.getNomePaciente(),
                plano.getIdade(),
                plano.getPesoAtual(),
                plano.getObjetivo(),
                plano.getIntensidadeExercicio()
        ));

        sb.append(String.format(
                "🔥 **ENERGÉTICOS**\n" +
                "├─ TMB: %.0f kcal/dia\n" +
                "├─ Gasto: %.0f kcal/dia\n" +
                "└─ Meta: %.0f kcal/dia\n\n",
                plano.getTmb(),
                plano.getGastoDiario(),
                plano.getCaloriaAlvo()
        ));

        // Macronutrientes
        Macronutrientes macro = plano.getMacronutrientes();
        if (macro != null) {
            sb.append(String.format(
                    "🥗 **MACRONUTRIENTES**\n" +
                    "├─ Proteína: %.1fg (%.0f kcal - %.1f%%)\n" +
                    "├─ Carbo: %.1fg (%.0f kcal - %.1f%%)\n" +
                    "└─ Gordura: %.1fg (%.0f kcal - %.1f%%)\n\n",
                    macro.getProteinaGramas(), macro.getProteinaCalorias(), macro.getProteinaPercentual(),
                    macro.getCarboidratoGramas(), macro.getCarboIdratoCalorias(), macro.getCarboidratoPercentual(),
                    macro.getGorduraGramas(), macro.getGorduraCalorias(), macro.getGorduraPercentual()
            ));
        }

        sb.append("📌 **RECOMENDAÇÕES**\n");
        if (plano.getRecomendacoes() != null && !plano.getRecomendacoes().isEmpty()) {
            for (String rec : plano.getRecomendacoes()) {
                sb.append("├─ ").append(rec).append("\n");
            }
        } else {
            sb.append("├─ Nenhuma recomendação específica\n");
        }

        if (plano.getValidadeDias() != null) {
            sb.append(String.format("\n✓ Validade: %d dias\n", plano.getValidadeDias()));
        }

        sb.append("✓ Microserviço: Independente (sem dependências externas)\n");

        return sb.toString();
    }
}