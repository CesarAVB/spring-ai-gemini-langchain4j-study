package br.com.sistema.springaigemini.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repositorio {
    
    private Integer id;
    private String nome;
    private String descricao;
    private String url;
    private String linguagem;
    private Integer stars;
    private Integer forks;
    private Integer issuesAbertas;
    private String dataCriacao; // ISO format
    private String dataAtualizacao; // ISO format
    private Boolean privado;
}