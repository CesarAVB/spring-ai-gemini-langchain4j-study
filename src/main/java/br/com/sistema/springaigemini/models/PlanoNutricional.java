package br.com.sistema.springaigemini.models;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo interno do PlanoNutricional.
 * 
 * Este é um POJO simples que não depende de nenhuma entidade externa.
 * É usado internamente para cálculos e convertido para DTO via MapperStruct.
 * 
 * Pode ser persistido em um banco local (opcional) adicionando @Entity.
 */
public class PlanoNutricional {

    private Long pacienteId;
    private String nomePaciente;
    private Double alturaMetros;
    private Double pesoAtual;
    private Integer idade;
    private String objetivo;
    private String intensidadeExercicio;
    private Double tmb;
    private Double gastoDiario;
    private Double caloriaAlvo;
    private Macronutrientes macronutrientes;
    private List<String> recomendacoes;
    private LocalDate dataCalculo;
    private Integer validadeDias;
    private String explicacaoCalculo;

    // Construtores
    public PlanoNutricional() {
    }

    public PlanoNutricional(Long pacienteId, String nomePaciente, Double alturaMetros,
                           Double pesoAtual, Integer idade, String objetivo,
                           String intensidadeExercicio, Double tmb, Double gastoDiario,
                           Double caloriaAlvo, Macronutrientes macronutrientes,
                           List<String> recomendacoes, LocalDate dataCalculo,
                           Integer validadeDias, String explicacaoCalculo) {
        this.pacienteId = pacienteId;
        this.nomePaciente = nomePaciente;
        this.alturaMetros = alturaMetros;
        this.pesoAtual = pesoAtual;
        this.idade = idade;
        this.objetivo = objetivo;
        this.intensidadeExercicio = intensidadeExercicio;
        this.tmb = tmb;
        this.gastoDiario = gastoDiario;
        this.caloriaAlvo = caloriaAlvo;
        this.macronutrientes = macronutrientes;
        this.recomendacoes = recomendacoes;
        this.dataCalculo = dataCalculo;
        this.validadeDias = validadeDias;
        this.explicacaoCalculo = explicacaoCalculo;
    }

    // Getters e Setters
    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public Double getAlturaMetros() {
        return alturaMetros;
    }

    public void setAlturaMetros(Double alturaMetros) {
        this.alturaMetros = alturaMetros;
    }

    public Double getPesoAtual() {
        return pesoAtual;
    }

    public void setPesoAtual(Double pesoAtual) {
        this.pesoAtual = pesoAtual;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getIntensidadeExercicio() {
        return intensidadeExercicio;
    }

    public void setIntensidadeExercicio(String intensidadeExercicio) {
        this.intensidadeExercicio = intensidadeExercicio;
    }

    public Double getTmb() {
        return tmb;
    }

    public void setTmb(Double tmb) {
        this.tmb = tmb;
    }

    public Double getGastoDiario() {
        return gastoDiario;
    }

    public void setGastoDiario(Double gastoDiario) {
        this.gastoDiario = gastoDiario;
    }

    public Double getCaloriaAlvo() {
        return caloriaAlvo;
    }

    public void setCaloriaAlvo(Double caloriaAlvo) {
        this.caloriaAlvo = caloriaAlvo;
    }

    public Macronutrientes getMacronutrientes() {
        return macronutrientes;
    }

    public void setMacronutrientes(Macronutrientes macronutrientes) {
        this.macronutrientes = macronutrientes;
    }

    public List<String> getRecomendacoes() {
        return recomendacoes;
    }

    public void setRecomendacoes(List<String> recomendacoes) {
        this.recomendacoes = recomendacoes;
    }

    public LocalDate getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDate dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public Integer getValidadeDias() {
        return validadeDias;
    }

    public void setValidadeDias(Integer validadeDias) {
        this.validadeDias = validadeDias;
    }

    public String getExplicacaoCalculo() {
        return explicacaoCalculo;
    }

    public void setExplicacaoCalculo(String explicacaoCalculo) {
        this.explicacaoCalculo = explicacaoCalculo;
    }

    /**
     * Classe interna para representar Macronutrientes no modelo.
     */
    public static class Macronutrientes {
        private Double proteinaGramas;
        private Double proteinaCalorias;
        private Double proteinaPercentual;
        private Double carboidratoGramas;
        private Double carboIdratoCalorias;
        private Double carboidratoPercentual;
        private Double gorduraGramas;
        private Double gorduraCalorias;
        private Double gorduraPercentual;

        // Construtores
        public Macronutrientes() {
        }

        public Macronutrientes(Double proteinaGramas, Double proteinaCalorias, Double proteinaPercentual,
                              Double carboidratoGramas, Double carboIdratoCalorias, Double carboidratoPercentual,
                              Double gorduraGramas, Double gorduraCalorias, Double gorduraPercentual) {
            this.proteinaGramas = proteinaGramas;
            this.proteinaCalorias = proteinaCalorias;
            this.proteinaPercentual = proteinaPercentual;
            this.carboidratoGramas = carboidratoGramas;
            this.carboIdratoCalorias = carboIdratoCalorias;
            this.carboidratoPercentual = carboidratoPercentual;
            this.gorduraGramas = gorduraGramas;
            this.gorduraCalorias = gorduraCalorias;
            this.gorduraPercentual = gorduraPercentual;
        }

        // Getters e Setters
        public Double getProteinaGramas() {
            return proteinaGramas;
        }

        public void setProteinaGramas(Double proteinaGramas) {
            this.proteinaGramas = proteinaGramas;
        }

        public Double getProteinaCalorias() {
            return proteinaCalorias;
        }

        public void setProteinaCalorias(Double proteinaCalorias) {
            this.proteinaCalorias = proteinaCalorias;
        }

        public Double getProteinaPercentual() {
            return proteinaPercentual;
        }

        public void setProteinaPercentual(Double proteinaPercentual) {
            this.proteinaPercentual = proteinaPercentual;
        }

        public Double getCarboidratoGramas() {
            return carboidratoGramas;
        }

        public void setCarboidratoGramas(Double carboidratoGramas) {
            this.carboidratoGramas = carboidratoGramas;
        }

        public Double getCarboIdratoCalorias() {
            return carboIdratoCalorias;
        }

        public void setCarboIdratoCalorias(Double carboIdratoCalorias) {
            this.carboIdratoCalorias = carboIdratoCalorias;
        }

        public Double getCarboidratoPercentual() {
            return carboidratoPercentual;
        }

        public void setCarboidratoPercentual(Double carboidratoPercentual) {
            this.carboidratoPercentual = carboidratoPercentual;
        }

        public Double getGorduraGramas() {
            return gorduraGramas;
        }

        public void setGorduraGramas(Double gorduraGramas) {
            this.gorduraGramas = gorduraGramas;
        }

        public Double getGorduraCalorias() {
            return gorduraCalorias;
        }

        public void setGorduraCalorias(Double gorduraCalorias) {
            this.gorduraCalorias = gorduraCalorias;
        }

        public Double getGorduraPercentual() {
            return gorduraPercentual;
        }

        public void setGorduraPercentual(Double gorduraPercentual) {
            this.gorduraPercentual = gorduraPercentual;
        }
    }
}