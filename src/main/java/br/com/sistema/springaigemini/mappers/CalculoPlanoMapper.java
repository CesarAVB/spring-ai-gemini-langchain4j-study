package br.com.sistema.springaigemini.mappers;

import br.com.sistema.springaigemini.dtos.CalculoPlanoCompleteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para conversão de CalculoPlanoRequest (record).
 * 
 * Este mapper é simples pois o record já é um DTO.
 * Mantido aqui para padronização da arquitetura de mappers.
 */
@Mapper
public interface CalculoPlanoMapper {

    CalculoPlanoMapper INSTANCE = Mappers.getMapper(CalculoPlanoMapper.class);

    /**
     * Aqui você pode adicionar conversões customizadas se necessário.
     * Por enquanto, é um placeholder para manter consistência.
     */
    default CalculoPlanoCompleteRequest toDTO(CalculoPlanoCompleteRequest request) {
        return request; // Record é imutável, retorna como-é
    }
}