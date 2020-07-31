package com.practice.persistence.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.practice.persistence.entities.Employee;

@Component
public class EmployeeSpecification {

    public Specification<Employee> getEmployeeByIdSpec(String fieldName, long fieldValue) {
        return (Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(fieldName), fieldValue);
    }

    public Specification<Employee> getEmployeeByTextualSpec(String fieldName, String fieldValue) {
        return (Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get(fieldName)), fieldValue.toLowerCase());
    }

}
