package br.com.sistema.springaigemini.mappers.response.plano;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.response.plano.PlanoResponse;
import br.com.sistema.springaigemini.models.PlanoNutricional;

/**
 * Mapper para converter PlanoNutricional em PlanoResponse
 * 
 * MapperStruct mapeia automaticamente todos os campos com mesmo nome
 */
@Mapper(componentModel = "spring")
public interface PlanoResponseMapper {

    PlanoResponseMapper INSTANCE = Mappers.getMapper(PlanoResponseMapper.class);

    /**
     * Converte PlanoNutricional para PlanoResponse
     * 
     * Mapeia automaticamente:
     * - id → id
     * - nome → nome
     * - idade → idade
     * - pesoAtual → pesoAtual
     * - recomendacoes (List<String>) → recomendacoes (List<String>)
     * - objetivo → objetivo
     * - intensidadeExercicio → intensidadeExercicio
     */
    PlanoResponse toPlanoResponse(PlanoNutricional plano);
}