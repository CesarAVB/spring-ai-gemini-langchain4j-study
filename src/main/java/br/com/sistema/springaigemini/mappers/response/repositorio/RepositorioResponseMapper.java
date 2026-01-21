package br.com.sistema.springaigemini.mappers.response.repositorio;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.response.repositorio.RepositorioInfoResponse;
import br.com.sistema.springaigemini.dtos.response.repositorio.RepositorioResponse;
import br.com.sistema.springaigemini.models.Repositorio;

/**
 * Mapper para converter Repositorio (entity/model) em RepositorioResponse (DTO)
 * 
 * MapperStruct gera a implementação automaticamente
 */
@Mapper(componentModel = "spring")
public interface RepositorioResponseMapper {
    
    RepositorioResponseMapper INSTANCE = Mappers.getMapper(RepositorioResponseMapper.class);
    
    /**
     * Converte Entity Repositorio para RepositorioInfoResponse (DTO)
     */
    RepositorioInfoResponse toRepositorioInfoResponse(Repositorio repositorio);
    
    /**
     * Converte lista de Repositorios para RepositorioResponse completo
     */
    default RepositorioResponse toRepositorioResponse(List<Repositorio> repositorios) {
        if (repositorios == null) {
            return new RepositorioResponse(0, List.of());
        }
        
        List<RepositorioInfoResponse> reposResponse = repositorios.stream()
            .map(this::toRepositorioInfoResponse)
            .toList();
        
        return new RepositorioResponse(reposResponse.size(), reposResponse);
    }
}