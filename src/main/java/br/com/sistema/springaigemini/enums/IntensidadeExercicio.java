package br.com.sistema.springaigemini.enums;

public enum IntensidadeExercicio {
    SEDENTARIO("sedentário", 1.2),
    LEVE("leve", 1.375),
    MODERADO("moderado", 1.55),
    INTENSO("intenso", 1.725),
    MUITO_INTENSO("muito_intenso", 1.9);

    private final String descricao;
    private final Double fatorAtividade;

    IntensidadeExercicio(String descricao, Double fatorAtividade) {
        this.descricao = descricao;
        this.fatorAtividade = fatorAtividade;
    }

    public String getDescricao() {
        return descricao;
    }

    public Double getFatorAtividade() {
        return fatorAtividade;
    }

    public static IntensidadeExercicio fromString(String valor) {
        for (IntensidadeExercicio intensidade : IntensidadeExercicio.values()) {
            if (intensidade.descricao.equalsIgnoreCase(valor)) {
                return intensidade;
            }
        }
        return MODERADO;
    }
}