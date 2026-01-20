package br.com.sistema.springaigemini.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.sistema.springaigemini.dtos.MacronutrientesDTO;
import br.com.sistema.springaigemini.models.PlanoNutricional;

/**
 * Mapper para conversão entre Macronutrientes (model) e MacronutrientesDTO.
 * 
 * MapperStruct gera automaticamente a implementação em tempo de compilação.
 */
@Mapper
public interface MacronutrientesMapper {

    MacronutrientesMapper INSTANCE = Mappers.getMapper(MacronutrientesMapper.class);

    /**
     * Converte Macronutrientes (model) para MacronutrientesDTO (record).
     */
    MacronutrientesDTO toDTO(PlanoNutricional.Macronutrientes macronutrientes);

    /**
     * Converte MacronutrientesDTO (record) para Macronutrientes (model).
     */
    PlanoNutricional.Macronutrientes toModel(MacronutrientesDTO dto);
}