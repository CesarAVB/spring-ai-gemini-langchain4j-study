package br.com.sistema.springaigemini.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry centralizado para todos os assistentes do sistema.
 * 
 * Facilita:
 * - Listar assistentes disponíveis
 * - Obter um assistente específico pelo nome
 * - Descoberta automática de assistentes anotados com @Service
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AssistantRegistry {

    private final List<GenericAssistant> assistants;
    private final Map<String, GenericAssistant> assistantMap = new HashMap<>();

    /**
     * Inicializa o registry e registra todos os assistentes descobertos.
     */
    public void initialize() {
        assistants.forEach(assistant -> {
            String name = assistant.getAssistantName();
            assistantMap.put(name.toLowerCase(), assistant);
            log.info("✅ Assistente registrado: {} - {}", name, assistant.getDescription());
        });
        log.info("📊 Total de assistentes disponíveis: {}", assistantMap.size());
    }

    /**
     * Obtém um assistente pelo nome (case-insensitive).
     */
    public Optional<GenericAssistant> getAssistant(String name) {
        return Optional.ofNullable(assistantMap.get(name.toLowerCase()));
    }

    /**
     * Retorna lista de todos os assistentes disponíveis.
     */
    public List<String> listAssistants() {
        return assistants.stream()
                .map(a -> String.format("%s - %s", a.getAssistantName(), a.getDescription()))
                .toList();
    }

    /**
     * Retorna a quantidade de assistentes registrados.
     */
    public int getAssistantCount() {
        return assistantMap.size();
    }
}