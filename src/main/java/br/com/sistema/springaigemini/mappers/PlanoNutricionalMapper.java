package br.com.sistema.springaigemini.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.PlanoNutricionalDTO;
import br.com.sistema.springaigemini.models.PlanoNutricional;

/**
 * Mapper para conversão entre PlanoNutricional (model) e PlanoNutricionalDTO (record).
 * 
 * MapperStruct gera automaticamente a implementação em tempo de compilação.
 * O campo macronutrientes é mapeado automaticamente via MacronutrientesMapper.
 */
@Mapper(uses = {MacronutrientesMapper.class})
public interface PlanoNutricionalMapper {

    PlanoNutricionalMapper INSTANCE = Mappers.getMapper(PlanoNutricionalMapper.class);

    /**
     * Converte PlanoNutricional (model) para PlanoNutricionalDTO (record).
     * 
     * MapperStruct automaticamente:
     * - Mapeia todos os campos por nome
     * - Usa MacronutrientesMapper para o campo macronutrientes
     */
    PlanoNutricionalDTO toDTO(PlanoNutricional plano);

    /**
     * Converte PlanoNutricionalDTO (record) para PlanoNutricional (model).
     */
    PlanoNutricional toModel(PlanoNutricionalDTO dto);
}