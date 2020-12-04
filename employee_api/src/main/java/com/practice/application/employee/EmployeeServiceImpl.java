package com.practice.application.employee;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.application.shared.ServiceExceptionHandler;
import com.practice.domain.employee.Employee;
import com.practice.domain.employee.EmployeeRepository;
import com.practice.domain.employee.EmployeeSpecification;
import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;
import com.practice.interfaces.rest.validators.FieldCriteriaValidator;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String ALL_EMPLOYEES_CACHE = "EmployeeService::getEmployees()";

    private static final String FILTERED_EMPLOYEES_CACHE_BY_CRITERIA = "EmployeeService::getEmployeeByFieldCriteria()";

    private final EmployeeRepository employeeRepository;

    private final EmployeeSpecification employeeSpecification;

    private final RateAlertRepository rateAlertRepository;

    private final ServiceExceptionHandler serviceExceptionHandler;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeSpecification employeeSpecification,
                               RateAlertRepository rateAlertRepository, ServiceExceptionHandler serviceExceptionHandler) {
        this.employeeRepository = employeeRepository;
        this.employeeSpecification = employeeSpecification;
        this.rateAlertRepository = rateAlertRepository;
        this.serviceExceptionHandler = serviceExceptionHandler;
    }

    @Cacheable(ALL_EMPLOYEES_CACHE)
    @Override
    public List<SavedEmployeeDto> getEmployees() {
        List<SavedEmployeeDto> result = employeeRepository.findAllSavedEmployees();
        if (result.isEmpty()) {
            throw new NoResultException();
        }
        return result;
    }

    @Cacheable(FILTERED_EMPLOYEES_CACHE_BY_CRITERIA)
    @Override
    public Employee getEmployeeByFieldCriteria(String fieldName, String fieldValue) {
        Specification<Employee> specification = null;
        switch (fieldName) {
            case FieldCriteriaValidator.ID:
                specification = employeeSpecification.getEmployeeByIdSpec(fieldName, Long.parseLong(fieldValue));
                break;
            case FieldCriteriaValidator.EMAIL:
            case FieldCriteriaValidator.USERNAME:
                specification = employeeSpecification.getEmployeeByTextualSpec(fieldName, fieldValue);
                break;
        }
        return employeeRepository.findOne(specification)
                                    .orElseThrow(EntityNotFoundException::new);
    }

    @CacheEvict(value = ALL_EMPLOYEES_CACHE, allEntries = true)
    @Transactional
    @Override
    public long createEmployee(Employee employee) {
        try {
            return employeeRepository.save(employee).getId();
        } catch (DataIntegrityViolationException e) {
            throw serviceExceptionHandler.wrapDataIntegrityViolationException(e, Employee.class);
        }
    }

    @CacheEvict(value = { ALL_EMPLOYEES_CACHE, FILTERED_EMPLOYEES_CACHE_BY_CRITERIA }, allEntries = true)
    @Transactional
    @Override
    public void updateEmployeeEmailById(long id, String email) {
        try {
            Employee employee = employeeRepository.getOne(id);
            employee.setEmail(email);
            employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException e) {
            throw serviceExceptionHandler.wrapDataIntegrityViolationException(e, Employee.class);
        } catch (javax.persistence.EntityNotFoundException e) {
            throw new EntityNotFoundException();
        }
    }

    @CacheEvict(value = { ALL_EMPLOYEES_CACHE, FILTERED_EMPLOYEES_CACHE_BY_CRITERIA }, allEntries = true)
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
    public void registerForScheduledMailAlert(RateAlert rateAlert) {
        try {
            rateAlertRepository.save(rateAlert);
        } catch (DataIntegrityViolationException e) {
            throw serviceExceptionHandler.wrapDataIntegrityViolationException(e, RateAlert.class);
        }
    }

}
