package br.com.sistema.springaigemini.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.sistema.springaigemini.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.springaigemini.dtos.PacienteDTO;
import br.com.sistema.springaigemini.dtos.PlanoNutricionalDTO;
import br.com.sistema.springaigemini.enums.IntensidadeExercicio;
import br.com.sistema.springaigemini.enums.ObjetivoNutricional;
import br.com.sistema.springaigemini.mappers.PlanoNutricionalMapper;
import br.com.sistema.springaigemini.models.PlanoNutricional;
import lombok.RequiredArgsConstructor;

/**
 * Service de cálculo de plano nutricional.
 * 
 * IMPORTANTE: Este service é independente e não depende de nenhuma entidade do projeto de nutrição.
 * 
 * Recebe DTOs como entrada (PacienteDTO, AvaliacaoFisicaDTO) que vêm de outro microserviço.
 * Realiza os cálculos internamente e retorna o resultado como PlanoNutricionalDTO.
 */
@Service
@RequiredArgsConstructor
public class PlanoNutricionalCalculatorService {

    private final PlanoNutricionalMapper planoMapper;

    /**
     * Calcula um plano nutricional personalizado baseado nos dados do paciente.
     * 
     * Este método é independente e recebe dados via DTOs, não dependendo de entidades locais.
     * 
     * @param pacienteDTO dados do paciente (vindo de outro microserviço)
     * @param avaliacaoDTO avaliação física mais recente
     * @param objetivo emagrecimento, manutenção ou ganho
     * @param intensidadeExercicio nível de atividade
     * @return DTO do plano calculado
     * @throws IllegalArgumentException se objetivo ou intensidade forem inválidos
     */
    public PlanoNutricionalDTO calcularPlano(
            PacienteDTO pacienteDTO,
            AvaliacaoFisicaDTO avaliacaoDTO,
            String objetivo,
            String intensidadeExercicio) {

        // Validação de entrada
        if (pacienteDTO == null) {
            throw new IllegalArgumentException("Dados do paciente não podem ser nulos");
        }
        if (avaliacaoDTO == null) {
            throw new IllegalArgumentException("Avaliação física não pode ser nula");
        }

        // Conversão de strings para enums com fallback
        ObjetivoNutricional objEnum = ObjetivoNutricional.fromString(objetivo);
        IntensidadeExercicio intensEnum = IntensidadeExercicio.fromString(intensidadeExercicio);

        // Cálculos básicos
        Integer idade = calcularIdade(pacienteDTO.dataNascimento());
        Double pesoKg = avaliacaoDTO.pesoAtual();
        Double alturaMetros = pacienteDTO.altura();
        Integer alturaCmd = (int) (alturaMetros * 100);
        Boolean ehHomem = pacienteDTO.sexo().equalsIgnoreCase("M");

        // Cálculos energéticos
        Double tmb = calcularTMB(pesoKg, alturaCmd, idade, ehHomem);
        Double gastoDiario = tmb * intensEnum.getFatorAtividade();
        Double caloriaAlvo = gastoDiario * objEnum.getFatorAjuste();

        // Distribuição de macronutrientes
        PlanoNutricional.Macronutrientes macros = distribuirMacronutrientes(pesoKg, caloriaAlvo, objEnum);

        // Recomendações personalizadas
        List<String> recomendacoes = gerarRecomendacoes(objEnum, intensEnum, pesoKg);

        // Explicação dos cálculos
        String explicacao = montarExplicacao(tmb, gastoDiario, caloriaAlvo, intensEnum, objEnum);

        // Criar modelo intermediário
        PlanoNutricional plano = new PlanoNutricional(
                pacienteDTO.id(),
                pacienteDTO.nome(),
                alturaMetros,
                Math.round(pesoKg * 10.0) / 10.0,
                idade,
                objEnum.getDescricao(),
                intensEnum.getDescricao(),
                Math.round(tmb * 10.0) / 10.0,
                Math.round(gastoDiario * 10.0) / 10.0,
                Math.round(caloriaAlvo * 10.0) / 10.0,
                macros,
                recomendacoes,
                LocalDate.now(),
                30,
                explicacao
        );

        // Converter modelo para DTO via MapperStruct
        return planoMapper.toDTO(plano);
    }

    /**
     * Calcula a Taxa Metabólica Basal (TMB) usando fórmula de Harris-Benedict.
     * 
     * @param pesoKg peso em kg
     * @param alturaCmd altura em cm
     * @param idade idade em anos
     * @param ehHomem true se homem, false se mulher
     * @return TMB em kcal/dia
     */
    private Double calcularTMB(Double pesoKg, Integer alturaCmd, Integer idade, Boolean ehHomem) {
        if (ehHomem) {
            // Homem: TMB = (10 × peso) + (6.25 × altura) - (5 × idade) + 5
            return (10 * pesoKg) + (6.25 * alturaCmd) - (5 * idade) + 5;
        } else {
            // Mulher: TMB = (10 × peso) + (6.25 × altura) - (5 × idade) - 161
            return (10 * pesoKg) + (6.25 * alturaCmd) - (5 * idade) - 161;
        }
    }

    /**
     * Distribui macronutrientes baseado no objetivo nutricional.
     * 
     * @param pesoKg peso em kg
     * @param caloriaAlvo calorias alvo diárias
     * @param objetivo tipo de objetivo (emagrecimento, manutenção, ganho)
     * @return objeto com distribuição de macros
     */
    private PlanoNutricional.Macronutrientes distribuirMacronutrientes(
            Double pesoKg, Double caloriaAlvo, ObjetivoNutricional objetivo) {

        Double proteinaGramas;
        Double percentualGordura;
        Double percentualCarboidrato;

        switch (objetivo) {
            case GANHO_MASSA -> {
                proteinaGramas = pesoKg * 2.0;
                percentualGordura = 0.30;
                percentualCarboidrato = 0.50;
            }
            case EMAGRECIMENTO -> {
                proteinaGramas = pesoKg * 1.6;
                percentualGordura = 0.25;
                percentualCarboidrato = 0.45;
            }
            default -> {
                proteinaGramas = pesoKg * 1.8;
                percentualGordura = 0.28;
                percentualCarboidrato = 0.47;
            }
        }

        // Cálculos
        Double proteinaCalorias = proteinaGramas * 4;
        Double gorduraGramas = (caloriaAlvo * percentualGordura) / 9;
        Double gorduraCalorias = gorduraGramas * 9;
        Double carboidratoCalorias = caloriaAlvo - proteinaCalorias - gorduraCalorias;
        Double carboidratoGramas = carboidratoCalorias / 4;

        return new PlanoNutricional.Macronutrientes(
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
     * Gera recomendações personalizadas baseadas no objetivo.
     */
    private List<String> gerarRecomendacoes(ObjetivoNutricional objetivo, IntensidadeExercicio intensidade, Double pesoKg) {
        return switch (objetivo) {
            case EMAGRECIMENTO -> Arrays.asList(
                    "Aumentar ingestão de água: mínimo 3 litros por dia",
                    "Distribuir proteína em 4-5 refeições para melhor absorção",
                    "Priorizar fibras (alimentos integrais, frutas, verduras)",
                    "Reduzir alimentos ultraprocessados e bebidas açucaradas",
                    "Criar déficit calórico consistente com exercício regular"
            );
            case GANHO_MASSA -> Arrays.asList(
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
                                   IntensidadeExercicio intensidade, ObjetivoNutricional objetivo) {
        return String.format(
                "Cálculo realizado por fórmula de Harris-Benedict. TMB: %.0f kcal. " +
                "Com fator de atividade de %.2f (intensidade: %s), gasto diário é %.0f kcal. " +
                "Aplicado ajuste de %s, resultando em %.0f kcal como meta diária.",
                tmb, intensidade.getFatorAtividade(), intensidade.getDescricao(),
                gastoDiario, objetivo.getDescricao(), caloriaAlvo
        );
    }

    /**
     * Calcula idade a partir da data de nascimento.
     */
    private Integer calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula");
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}