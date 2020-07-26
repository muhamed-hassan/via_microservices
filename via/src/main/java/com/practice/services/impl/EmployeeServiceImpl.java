package com.practice.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.configs.CachingConfig;
import com.practice.exceptions.EntityModificationException;
import com.practice.exceptions.EntityNotFoundException;
import com.practice.exceptions.handlers.EmployeeServiceErrorHandler;
import com.practice.persistence.entities.EmployeeEntity;
import com.practice.persistence.repositories.EmployeeRepository;
import com.practice.persistence.specs.EmployeeSpecification;
import com.practice.services.EmployeeService;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.EmployeeDto;

@CacheConfig(cacheNames = CachingConfig.Stores.EMPLOYEES)
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityTransformer entityTransformer;

    @Autowired
    private EmployeeSpecification employeeSpecification;

    @Autowired
    private EmployeeServiceErrorHandler employeeServiceErrorHandler;

    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Override
    public List<EmployeeEntity> getEmployees() {
        return employeeRepository.findAll();
    }

    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Override
    public EmployeeEntity getEmployeeByFieldCriteria(final String fieldCriteria) {
        String[] criterionTokens = fieldCriteria.split(":");
        if (criterionTokens != null && criterionTokens.length == 2) {
            String fieldName = criterionTokens[0].toLowerCase();
            String fieldValue = criterionTokens[1];
            Specification<EmployeeEntity> specification = null;
            if ("id".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByIdSpec(fieldName, Long.parseLong(fieldValue));
            } else if ("email".equals(fieldName) || "username".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByTextualSpec(fieldName, fieldValue);
            } else {
                throw new UnsupportedOperationException("Invalid criteria, allowed criteria are id, email, username ONLY");
            }
            return employeeRepository.findOne(specification)
                                        .orElseThrow(EntityNotFoundException::new);
        } else {
            throw new IllegalArgumentException("Invalid criteria format, it should be in the form of fieldName:fieldValue");
        }
    }

    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Transactional
    @Override
    public long createEmployee(final EmployeeDto employeeDto) {
        long createdEmployeeId = -1;
        try {
            employeeDto.setId(0);
            createdEmployeeId = employeeRepository.save(entityTransformer.toEntity(employeeDto, EmployeeEntity.class)).getId();
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMostSpecificCause().getMessage());
            employeeServiceErrorHandler.handleDataIntegrityViolationException(e);
        }
        return createdEmployeeId;
    }

    @CachePut(value = CachingConfig.Stores.EMPLOYEES, key = "#id")
    @Transactional
    @Override
    public void updateEmployeeEmailById(final long id, final String email) {
        int affectedRows = -1;
        try {
            affectedRows = employeeRepository.updateEmail(id, email);
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMostSpecificCause().getMessage());
            employeeServiceErrorHandler.handleDataIntegrityViolationException(e);
        }
        if (affectedRows != 1) {
            logger.error("Failed to update this email:{}, with id:{}", email, id);
            throw new EntityModificationException();
        }
    }

    @CacheEvict(value = CachingConfig.Stores.EMPLOYEES, key = "#id")
    @Transactional
    @Override
    public void deleteEmployeeById(final long id) {
        try {
            employeeRepository.deleteById(id);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            logger.error("Record with id:{} does not exist", id);
            throw new EntityNotFoundException(emptyResultDataAccessException);
        }
    }

}
