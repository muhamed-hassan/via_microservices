package com.practice.persistence.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.practice.persistence.entities.EmployeeEntity;

@Component
public class EmployeeSpecification {

    public Specification<EmployeeEntity> getEmployeeByIdSpec(final String fieldName, final long fieldValue) {
        return (Root<EmployeeEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(fieldName), fieldValue);
    }

    public Specification<EmployeeEntity> getEmployeeByTextualSpec(final String fieldName, final String fieldValue) {
        return (Root<EmployeeEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get(fieldName)), fieldValue.toLowerCase());
    }

}
