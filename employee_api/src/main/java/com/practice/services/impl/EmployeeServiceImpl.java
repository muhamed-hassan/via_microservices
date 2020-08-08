package com.practice.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.exceptions.EntityNotFoundException;
import com.practice.exceptions.NoResultException;
import com.practice.persistence.entities.Employee;
import com.practice.persistence.entities.RateAlert;
import com.practice.persistence.repositories.EmployeeRepository;
import com.practice.persistence.repositories.RateAlertRepository;
import com.practice.persistence.specs.EmployeeSpecification;
import com.practice.services.EmployeeService;
import com.practice.services.utils.ServiceErrorHandler;
import com.practice.transfomers.DtoTransformer;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.NewEmployeeDto;
import com.practice.web.dtos.RateAlertDto;
import com.practice.web.dtos.SavedEmployeeDto;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RateAlertRepository rateAlertRepository;

    @Autowired
    private EntityTransformer entityTransformer;

    @Autowired
    private DtoTransformer dtoTransformer;

    @Autowired
    private EmployeeSpecification employeeSpecification;

    @Autowired
    private ServiceErrorHandler serviceErrorHandler;

    @Cacheable(value = "EmployeeService::getEmployees()")
    @Override
    public List<SavedEmployeeDto> getEmployees() {
        List<SavedEmployeeDto> result = employeeRepository.findAllSavedEmployees();
        if (result.isEmpty()) {
            throw new NoResultException();
        }
        return result;
    }

    @Cacheable(value = "EmployeeService::getEmployeeByFieldCriteria()")
    @Override
    public SavedEmployeeDto getEmployeeByFieldCriteria(String fieldCriteria) {
        String[] criterionTokens = fieldCriteria.split(":");
        if (criterionTokens != null && criterionTokens.length == 2) {
            String fieldName = criterionTokens[0].toLowerCase();
            String fieldValue = criterionTokens[1];
            Specification<Employee> specification;
            if ("id".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByIdSpec(fieldName, Long.parseLong(fieldValue));
            } else if ("email".equals(fieldName) || "username".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByTextualSpec(fieldName, fieldValue);
            } else {
                throw new UnsupportedOperationException("Invalid criteria allowed criteria are id, email and username in the form of fieldName:validValue");
            }
            Employee employee = employeeRepository.findOne(specification)
                                                    .orElseThrow(EntityNotFoundException::new);
            return dtoTransformer.toDto(employee, SavedEmployeeDto.class);
        } else {
            throw new IllegalArgumentException("Invalid criteria format, it should be in the form of fieldName:fieldValue");
        }
    }

    @CacheEvict(value = "EmployeeService::getEmployees()", allEntries = true)
    @Transactional
    @Override
    public long createEmployee(NewEmployeeDto employeeDto) {
        try {
            return employeeRepository.save(entityTransformer.toEntity(employeeDto, Employee.class)).getId();
        } catch (DataIntegrityViolationException e) {
            throw serviceErrorHandler.wrapDataIntegrityViolationException(e, Employee.class);
        }
    }

    @CacheEvict(value = { "EmployeeService::getEmployees()", "EmployeeService::getEmployeeByFieldCriteria()" }, allEntries = true)
    @Transactional
    @Override
    public void updateEmployeeEmailById(long id, String email) {
        try {
            Employee employee = employeeRepository.getOne(id);
            employee.setEmail(email);
            employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw serviceErrorHandler.wrapDataIntegrityViolationException(e, Employee.class);
        } catch (javax.persistence.EntityNotFoundException e) {
            throw new EntityNotFoundException();
        }
    }

    @CacheEvict(value = { "EmployeeService::getEmployees()", "EmployeeService::getEmployeeByFieldCriteria()" }, allEntries = true)
    @Transactional
    @Override
    public void deleteEmployeeById(long id) {
        try {
            employeeRepository.deleteById(id);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            throw new EntityNotFoundException(emptyResultDataAccessException);
        }
    }

    @Transactional
    @Override
    public void registerForScheduledMailAlert(RateAlertDto rateAlertDto) {
        try {
            rateAlertRepository.save(entityTransformer.toEntity(rateAlertDto, RateAlert.class));
        } catch (DataIntegrityViolationException e) {
            throw serviceErrorHandler.wrapDataIntegrityViolationException(e, RateAlert.class);
        }
    }

}
