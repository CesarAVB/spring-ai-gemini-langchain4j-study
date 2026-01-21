package br.com.sistema.springaigemini.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {
    
    private String id; // ID do email no Gmail
    private Integer numero; // Número sequencial
    private Remetente remetente;
    private String assunto;
    private String data; // ISO format: 2025-01-20T20:30:00Z
    private String preview;
    private Boolean naoLido;
    private Boolean importante;
}