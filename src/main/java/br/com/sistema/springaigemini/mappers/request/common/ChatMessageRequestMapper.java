package br.com.sistema.springaigemini.mappers.request.common;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.request.common.ChatMessageRequest;

/**
 * Mapper para ChatMessageRequest
 * 
 * Genérico - apenas valida que a mensagem não está vazia
 */
@Mapper(componentModel = "spring")
public interface ChatMessageRequestMapper {
    
    ChatMessageRequestMapper INSTANCE = Mappers.getMapper(ChatMessageRequestMapper.class);
    
    /**
     * Validação básica de request
     */
    default ChatMessageRequest validate(ChatMessageRequest request) {
        if (request == null || request.message() == null || request.message().trim().isEmpty()) {
            throw new IllegalArgumentException("Mensagem não pode estar vazia");
        }
        return request;
    }
}