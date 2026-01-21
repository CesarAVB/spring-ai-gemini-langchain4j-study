package br.com.sistema.springaigemini.models;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {
    
    private Integer id;
    private String nome;
    private String email;
    private String sexo; // M, F, O
    private Double altura; // em metros
    private String dataNascimento; // YYYY-MM-DD
    private String cpf;
    private String telefone;
    
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime dataAtualizacao = LocalDateTime.now();
    
    /**
     * Constructor para criar paciente sem ID
     */
    public Paciente(String nome, String email, String sexo, Double altura, String dataNascimento) {
        this();
        this.nome = nome;
        this.email = email;
        this.sexo = sexo;
        this.altura = altura;
        this.dataNascimento = dataNascimento;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    /**
     * Atualiza o timestamp
     */
    public void atualizar() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}