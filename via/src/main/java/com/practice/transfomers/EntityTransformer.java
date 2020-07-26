package com.practice.transfomers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class EntityTransformer {

    private Logger logger = LoggerFactory.getLogger(EntityTransformer.class);

    @SuppressWarnings("unchecked")
    public <E, DTO> E toEntity(final DTO dto, final Class<E> entityClass) {
        E entity = null;
        try {
            Class<?> clazz = Class.forName(entityClass.getName());
            entity = (E) clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
        } catch (Exception e) {
            logger.error("Failed to transform this Dto:{} into an Entity:{}", dto, entityClass.getName());
            throw new RuntimeException(e);
        }
        return entity;
    }

}
