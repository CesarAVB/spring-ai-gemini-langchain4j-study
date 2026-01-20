package br.com.sistema.springaigemini.enums;

public enum ObjetivoNutricional {
    EMAGRECIMENTO("emagrecimento", 0.85),
    MANUTENÇÃO("manutenção", 1.0),
    GANHO_MASSA("ganho_massa", 1.10);

    private final String descricao;
    private final Double fatorAjuste;

    ObjetivoNutricional(String descricao, Double fatorAjuste) {
        this.descricao = descricao;
        this.fatorAjuste = fatorAjuste;
    }

    public String getDescricao() {
        return descricao;
    }

    public Double getFatorAjuste() {
        return fatorAjuste;
    }

    public static ObjetivoNutricional fromString(String valor) {
        for (ObjetivoNutricional obj : ObjetivoNutricional.values()) {
            if (obj.descricao.equalsIgnoreCase(valor)) {
                return obj;
            }
        }
        return MANUTENÇÃO;
    }
}