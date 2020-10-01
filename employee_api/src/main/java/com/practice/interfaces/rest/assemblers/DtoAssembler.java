package com.practice.interfaces.rest.assemblers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class DtoAssembler {

    public <E, DTO> DTO toDto(E entity, Class<DTO> dtoClass) {
        DTO dto;
        try {
            Class<?> clazz = Class.forName(dtoClass.getName());
            dto = (DTO) clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dto;
    }

}
