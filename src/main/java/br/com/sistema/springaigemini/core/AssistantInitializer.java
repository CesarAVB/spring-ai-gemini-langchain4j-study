package br.com.sistema.springaigemini.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener que inicializa o AssistantRegistry quando a aplicação inicia.
 * 
 * Isso garante que todos os assistentes sejam descobertos e registrados
 * automaticamente no startup da aplicação.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AssistantInitializer {

    private final AssistantRegistry assistantRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAssistants() {
        log.info("🚀 Inicializando registry de assistentes...");
        assistantRegistry.initialize();
        log.info("✅ Registry de assistentes inicializado com sucesso");
    }
}