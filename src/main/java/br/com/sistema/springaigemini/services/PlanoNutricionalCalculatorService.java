package br.com.sistema.springaigemini.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.dtos.request.plano.CreatePlanoRequest;
import br.com.sistema.springaigemini.models.PlanoNutricional;
import br.com.sistema.springaigemini.models.PlanoNutricional.Macronutrientes;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Service de cálculo de plano nutricional.
 * 
 * Recebe CreatePlanoRequest como entrada.
 * Realiza cálculos e retorna PlanoNutricional com todos os dados calculados.
 * 
 * SEM banco de dados - totalmente em memória.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PlanoNutricionalCalculatorService {

    /**
     * Calcula um plano nutricional personalizado baseado nos dados do paciente.
     * 
     * @param request contém: nome, idade, pesoAtual, objetivo, intensidadeExercicio, recomendacoes
     * @return PlanoNutricional com cálculos completos
     * @throws IllegalArgumentException se dados forem inválidos
     */
    public PlanoNutricional calcularPlano(CreatePlanoRequest request) {
        
        log.info("Iniciando cálculo de plano nutricional para: {}", request.nome());
        
        // Validação de entrada
        if (request == null) {
            throw new IllegalArgumentException("Request não pode ser nulo");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do paciente é obrigatório");
        }
        if (request.idade() == null || request.idade() <= 0) {
            throw new IllegalArgumentException("Idade deve ser maior que 0");
        }
        if (request.pesoAtual() == null || request.pesoAtual() <= 0) {
            throw new IllegalArgumentException("Peso deve ser maior que 0");
        }
        if (request.objetivo() == null || request.objetivo().isBlank()) {
            throw new IllegalArgumentException("Objetivo é obrigatório");
        }
        if (request.intensidadeExercicio() == null || request.intensidadeExercicio().isBlank()) {
            throw new IllegalArgumentException("Intensidade de exercício é obrigatória");
        }

        // Extrair dados
        String nome = request.nome();
        Integer idade = request.idade();
        Double pesoAtual = request.pesoAtual();
        String objetivo = request.objetivo();
        String intensidadeExercicio = request.intensidadeExercicio();
        List<String> recomendacoes = request.recomendacoes() != null ? 
            request.recomendacoes() : gerarRecomendacoesDefault(objetivo);

        // Cálculos energéticos (TMB e gasto diário)
        Double tmb = calcularTMB(pesoAtual, idade);
        Double fatorIntensidade = obterFatorIntensidade(intensidadeExercicio);
        Double gastoDiario = tmb * fatorIntensidade;
        Double caloriaAlvo = ajustarPorObjetivo(gastoDiario, objetivo);

        // Distribuir macronutrientes
        Macronutrientes macros = distribuirMacronutrientes(pesoAtual, caloriaAlvo, objetivo);

        // Montar explicação
        String explicacao = montarExplicacao(tmb, gastoDiario, caloriaAlvo, intensidadeExercicio, objetivo);

        log.info("TMB: {} | Gasto: {} | Caloria Alvo: {}", tmb, gastoDiario, caloriaAlvo);

        // Criar e retornar PlanoNutricional com TODOS os campos
        PlanoNutricional plano = new PlanoNutricional(
            null,                          // pacienteId (null - sem BD)
            nome,                          // nomePaciente
            1.75,                          // alturaMetros (padrão, poderia vir do request)
            pesoAtual,                     // pesoAtual
            idade,                         // idade
            objetivo,                      // objetivo
            intensidadeExercicio,          // intensidadeExercicio
            tmb,                           // tmb
            gastoDiario,                   // gastoDiario
            caloriaAlvo,                   // caloriaAlvo
            macros,                        // macronutrientes
            recomendacoes,                 // recomendacoes
            LocalDate.now(),               // dataCalculo
            30,                            // validadeDias
            explicacao                     // explicacaoCalculo
        );

        log.info("✅ Plano calculado com sucesso para: {}", nome);
        
        return plano;
    }

    /**
     * Calcula a Taxa Metabólica Basal (TMB) usando fórmula simplificada.
     * 
     * @param pesoKg peso em kg
     * @param idade idade em anos
     * @return TMB em kcal/dia
     */
    private Double calcularTMB(Double pesoKg, Integer idade) {
        // Fórmula simplificada: TMB ≈ peso × 24 com ajuste por idade
        Double tmb = pesoKg * 24;
        
        // Ajuste por idade (reduz ~2% a cada 10 anos após 30)
        if (idade > 30) {
            Integer anosApos30 = idade - 30;
            Double reducao = (anosApos30 / 10.0) * 0.02;
            tmb = tmb * (1 - reducao);
        }
        
        return Math.round(tmb * 10.0) / 10.0;
    }

    /**
     * Obtém fator de intensidade baseado no nível de exercício.
     * 
     * @param intensidade sedentário, leve, moderado, intenso
     * @return fator multiplicador (1.2 a 1.9)
     */
    private Double obterFatorIntensidade(String intensidade) {
        return switch (intensidade.toLowerCase()) {
            case "sedentário", "sedentario" -> 1.2;
            case "leve" -> 1.375;
            case "moderado" -> 1.55;
            case "intenso", "muito_intenso" -> 1.9;
            default -> 1.4;
        };
    }

    /**
     * Ajusta calorias alvo baseado no objetivo.
     * 
     * @param gastoDiario gasto calórico diário
     * @param objetivo emagrecimento, manutenção, ganho_massa
     * @return calorias alvo ajustadas
     */
    private Double ajustarPorObjetivo(Double gastoDiario, String objetivo) {
        return switch (objetivo.toLowerCase()) {
            case "emagrecimento", "perda_peso" -> gastoDiario * 0.85; // 15% déficit
            case "ganho_massa", "ganho" -> gastoDiario * 1.10;         // 10% superávit
            default -> gastoDiario;                                     // manutenção
        };
    }

    /**
     * Distribui macronutrientes baseado no objetivo.
     * 
     * @param pesoKg peso em kg
     * @param caloriaAlvo calorias alvo diárias
     * @param objetivo tipo de objetivo
     * @return Macronutrientes calculados
     */
    private Macronutrientes distribuirMacronutrientes(Double pesoKg, Double caloriaAlvo, String objetivo) {
        
        Double proteinaGramas;
        Double percentualGordura;
        Double percentualCarboidrato;

        switch (objetivo.toLowerCase()) {
            case "ganho_massa", "ganho" -> {
                proteinaGramas = pesoKg * 2.0;  // 2g por kg
                percentualGordura = 0.30;        // 30% gordura
                percentualCarboidrato = 0.50;    // 50% carbs
            }
            case "emagrecimento", "perda_peso" -> {
                proteinaGramas = pesoKg * 1.6;  // 1.6g por kg
                percentualGordura = 0.25;        // 25% gordura
                percentualCarboidrato = 0.45;    // 45% carbs
            }
            default -> {
                proteinaGramas = pesoKg * 1.8;  // 1.8g por kg
                percentualGordura = 0.28;        // 28% gordura
                percentualCarboidrato = 0.47;    // 47% carbs
            }
        }

        // Cálculos
        Double proteinaCalorias = proteinaGramas * 4;  // proteína = 4 kcal/g
        Double gorduraGramas = (caloriaAlvo * percentualGordura) / 9;  // gordura = 9 kcal/g
        Double gorduraCalorias = gorduraGramas * 9;
        Double carboidratoCalorias = caloriaAlvo - proteinaCalorias - gorduraCalorias;
        Double carboidratoGramas = carboidratoCalorias / 4;  // carbs = 4 kcal/g

        return new Macronutrientes(
            Math.round(proteinaGramas * 10.0) / 10.0,
            Math.round(proteinaCalorias * 10.0) / 10.0,
            Math.round((proteinaCalorias / caloriaAlvo * 100) * 10.0) / 10.0,
            Math.round(carboidratoGramas * 10.0) / 10.0,
            Math.round(carboidratoCalorias * 10.0) / 10.0,
            Math.round((carboidratoCalorias / caloriaAlvo * 100) * 10.0) / 10.0,
            Math.round(gorduraGramas * 10.0) / 10.0,
            Math.round(gorduraCalorias * 10.0) / 10.0,
            Math.round((gorduraCalorias / caloriaAlvo * 100) * 10.0) / 10.0
        );
    }

    /**
     * Gera recomendações padrão baseadas no objetivo.
     * 
     * @param objetivo tipo de objetivo
     * @return lista de recomendações
     */
    private List<String> gerarRecomendacoesDefault(String objetivo) {
        return switch (objetivo.toLowerCase()) {
            case "emagrecimento", "perda_peso" -> Arrays.asList(
                "Aumentar ingestão de água: mínimo 3 litros por dia",
                "Distribuir proteína em 4-5 refeições para melhor absorção",
                "Priorizar fibras (alimentos integrais, frutas, verduras)",
                "Reduzir alimentos ultraprocessados e bebidas açucaradas",
                "Criar déficit calórico consistente com exercício regular"
            );
            case "ganho_massa", "ganho" -> Arrays.asList(
                "Consumir proteína alta em todas as refeições (25-35g)",
                "Preferir carboidratos complexos (aveia, batata doce, arroz integral)",
                "Comer a cada 3-4 horas para maximizar síntese proteica",
                "Aumentar superávit calórico gradualmente (200-300 kcal)",
                "Treino de força 4-5x por semana para ganho otimizado"
            );
            default -> Arrays.asList(
                "Manter consumo equilibrado de macronutrientes",
                "Distribuir refeições de 3 em 3 horas",
                "Beber 2-3 litros de água por dia",
                "Incluir fonte de proteína em todas as refeições",
                "Praticar exercício físico moderado 3-5x por semana"
            );
        };
    }

    /**
     * Monta explicação textual dos cálculos realizados.
     */
    private String montarExplicacao(Double tmb, Double gastoDiario, Double caloriaAlvo,
                                   String intensidade, String objetivo) {
        return String.format(
            "Cálculo realizado por fórmula simplificada. TMB: %.0f kcal. " +
            "Com fator de atividade de %.2f (intensidade: %s), gasto diário é %.0f kcal. " +
            "Aplicado ajuste de %s, resultando em %.0f kcal como meta diária.",
            tmb, obterFatorIntensidade(intensidade), intensidade,
            gastoDiario, objetivo, caloriaAlvo
        );
    }
}