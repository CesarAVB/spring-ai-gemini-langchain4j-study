package br.com.sistema.springaigemini.mappers.response.email;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.response.email.EmailInfoResponse;
import br.com.sistema.springaigemini.dtos.response.email.EmailResponse;
import br.com.sistema.springaigemini.dtos.response.email.RemetenteResponse;
import br.com.sistema.springaigemini.models.Email;
import br.com.sistema.springaigemini.models.Remetente;

/**
 * Mapper para converter Email (entity/model) em EmailResponse (DTO)
 * 
 * MapperStruct gera a implementação automaticamente
 */
@Mapper(componentModel = "spring")
public interface EmailResponseMapper {
    
    EmailResponseMapper INSTANCE = Mappers.getMapper(EmailResponseMapper.class);
    
    /**
     * Converte Entity Email para EmailInfoResponse (DTO)
     */
    EmailInfoResponse toEmailInfoResponse(Email email);
    
    /**
     * Converte Entity Remetente para RemetenteResponse (DTO)
     */
    RemetenteResponse toRemetenteResponse(Remetente remetente);
    
    /**
     * Converte lista de Emails para EmailResponse completo
     */
    default EmailResponse toEmailResponse(List<Email> emails) {
        if (emails == null) {
            return new EmailResponse(0, List.of());
        }
        
        List<EmailInfoResponse> emailsResponse = emails.stream()
            .map(this::toEmailInfoResponse)
            .toList();
        
        return new EmailResponse(emailsResponse.size(), emailsResponse);
    }
}