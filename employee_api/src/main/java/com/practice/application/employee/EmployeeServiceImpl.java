package com.practice.application.employee;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.application.shared.ServiceErrorHandler;
import com.practice.domain.employee.Employee;
import com.practice.domain.employee.EmployeeRepository;
import com.practice.domain.employee.EmployeeSpecification;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeSpecification employeeSpecification;

    private final ServiceErrorHandler serviceErrorHandler;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                EmployeeSpecification employeeSpecification,
                                ServiceErrorHandler serviceErrorHandler) {
        this.employeeRepository = employeeRepository;
        this.employeeSpecification = employeeSpecification;
        this.serviceErrorHandler = serviceErrorHandler;
    }

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
    public Employee getEmployeeByFieldCriteria(String fieldCriteria) {
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
            return employeeRepository.findOne(specification)
                                        .orElseThrow(EntityNotFoundException::new);
        } else {
            throw new IllegalArgumentException("Invalid criteria format, it should be in the form of fieldName:fieldValue");
        }
    }

    @CacheEvict(value = "EmployeeService::getEmployees()", allEntries = true)
    @Transactional
    @Override
    public long createEmployee(Employee employee) {
        try {
            return employeeRepository.save(employee).getId();
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

}
