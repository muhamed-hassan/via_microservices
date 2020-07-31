package com.practice.transfomers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class EntityTransformer {

    public <E, DTO> E toEntity(DTO dto, Class<E> entityClass) {
        E entity;
        try {
            Class<?> clazz = Class.forName(entityClass.getName());
            entity = (E) clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

}
