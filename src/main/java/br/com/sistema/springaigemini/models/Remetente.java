package br.com.sistema.springaigemini.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remetente {
    
    private String nome;
    private String email;
}