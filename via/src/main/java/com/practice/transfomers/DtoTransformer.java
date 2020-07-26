package com.practice.transfomers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class DtoTransformer {

    private Logger logger = LoggerFactory.getLogger(DtoTransformer.class);

    @SuppressWarnings("unchecked")
    public <E, DTO> DTO toDto(final E entity, final Class<DTO> dtoClass) {
        DTO dto = null;
        try {
            Class<?> clazz = Class.forName(dtoClass.getName());
            dto = (DTO) clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, dto);
        } catch (Exception e) {
            logger.error("Failed to transform this Entity:{} into a Dto:{}", entity, dtoClass.getName());
            throw new RuntimeException(e);
        }
        return dto;
    }

}
