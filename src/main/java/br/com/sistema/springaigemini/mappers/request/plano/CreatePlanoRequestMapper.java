package br.com.sistema.springaigemini.mappers.request.plano;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.request.plano.CreatePlanoRequest;
import br.com.sistema.springaigemini.models.PlanoNutricional;

/**
 * Mapper para converter CreatePlanoRequest em PlanoNutricional
 * 
 * MapperStruct mapeia automaticamente todos os campos com mesmo nome
 */
@Mapper(componentModel = "spring")
public interface CreatePlanoRequestMapper {

    CreatePlanoRequestMapper INSTANCE = Mappers.getMapper(CreatePlanoRequestMapper.class);

    /**
     * Converte CreatePlanoRequest para PlanoNutricional
     * 
     * Mapeia automaticamente:
     * - nome → nome
     * - idade → idade
     * - pesoAtual → pesoAtual
     * - recomendacoes (List<String>) → recomendacoes (List<String>)
     * - objetivo → objetivo
     * - intensidadeExercicio → intensidadeExercicio
     */
    PlanoNutricional toEntity(CreatePlanoRequest request);
}