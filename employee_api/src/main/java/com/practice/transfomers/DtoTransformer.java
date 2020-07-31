package com.practice.transfomers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class DtoTransformer {

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
