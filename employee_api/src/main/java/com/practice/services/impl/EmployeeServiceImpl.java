package com.practice.services.impl;

import static com.practice.persistence.entities.Employee.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL;
import static com.practice.persistence.entities.Employee.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER;
import static com.practice.persistence.entities.Employee.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME;
import static com.practice.persistence.entities.RateAlert.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import org.bouncycastle.util.io.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practice.configs.constants.Messages;
import com.practice.exceptions.DbConstraintViolationException;
import com.practice.exceptions.EntityNotFoundException;
import com.practice.exceptions.NoResultException;
import com.practice.persistence.entities.Employee;
import com.practice.persistence.repositories.EmployeeRepository;
import com.practice.persistence.specs.EmployeeSpecification;
import com.practice.services.EmployeeService;
import com.practice.services.ServiceErrorHandler;
import com.practice.transfomers.DtoTransformer;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.NewEmployeeDto;
import com.practice.web.dtos.SavedEmployeeDto;

//@CacheConfig(cacheNames = CachingConfig.Stores.EMPLOYEES)
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityTransformer entityTransformer;

    @Autowired
    private DtoTransformer dtoTransformer;

    @Autowired
    private EmployeeSpecification employeeSpecification;

    @Autowired
    private ServiceErrorHandler serviceErrorHandler;

//    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Override
    public List<SavedEmployeeDto> getEmployees() {
        List<SavedEmployeeDto> result = employeeRepository.findAllSavedEmployees();
        if (result.isEmpty()) {
            throw new NoResultException();
        }
        return result;
    }
// TODO refactor this shit
//    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Override
    public SavedEmployeeDto getEmployeeByFieldCriteria(String fieldCriteria) {
        String[] criterionTokens = fieldCriteria.split(":");
        if (criterionTokens != null && criterionTokens.length == 2) {
            String fieldName = criterionTokens[0].toLowerCase();
            String fieldValue = criterionTokens[1];
            Specification<Employee> specification = null;
            if ("id".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByIdSpec(fieldName, Long.parseLong(fieldValue));
            } else if ("email".equals(fieldName) || "username".equals(fieldName)) {
                specification = employeeSpecification.getEmployeeByTextualSpec(fieldName, fieldValue);
            } else {
                throw new UnsupportedOperationException("Invalid criteria allowed criteria are id, email and username in the form of fieldName:validValue");
            }
            return dtoTransformer.toDto(employeeRepository.findOne(specification)
                                        .orElseThrow(EntityNotFoundException::new), SavedEmployeeDto.class);
        } else {
            throw new IllegalArgumentException("Invalid criteria format, it should be in the form of fieldName:fieldValue");
        }
    }


//    @CachePut(value = CachingConfig.Stores.EMPLOYEES)
    @Transactional
    @Override
    public long createEmployee(NewEmployeeDto employeeDto) {
        try {
            return employeeRepository.save(entityTransformer.toEntity(employeeDto, Employee.class)).getId();
        } catch (DataIntegrityViolationException e) {
            // ^.*_name_.*$
            throw serviceErrorHandler.wrapDataIntegrityViolationException( e, Employee.class);
        }
    }



//    @CachePut(value = CachingConfig.Stores.EMPLOYEES, key = "#id")
    @Transactional
    @Override
    public void updateEmployeeEmailById(long id, String email) {
        try {
            Employee employee = employeeRepository.getOne(id);
            employee.setEmail(email);
            employeeRepository.save(employee);
        } catch (DataIntegrityViolationException e) {
//            throw wrapDataIntegrityViolationException(e);
            throw new IllegalArgumentException("DB constraint is violated for this field: email");
        } catch (javax.persistence.EntityNotFoundException e) {
            throw new EntityNotFoundException();
        }
    }

//    @CacheEvict(value = CachingConfig.Stores.EMPLOYEES, key = "#id")
    @Transactional
    @Override
    public void deleteEmployeeById(long id) {
        try {
            employeeRepository.deleteById(id);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            throw new EntityNotFoundException(emptyResultDataAccessException);
        }
    }

//    private DbConstraintViolationException wrapDataIntegrityViolationException(DataIntegrityViolationException e) {
//        String exceptionMessage = e.getMostSpecificCause().getMessage();
//        String errorMsg = null;
//        if (exceptionMessage != null) {
//            String lowerCaseExceptionMessage = exceptionMessage.toLowerCase();
//            if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME)) {
//                errorMsg = Messages.USER_NAME_ALREADY_EXIST;
//            } else if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL)) {
//                errorMsg = Messages.EMAIL_ALREADY_EXIST;
//            } else if (lowerCaseExceptionMessage.contains(EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER)) {
//                errorMsg = Messages.PHONE_NUMBER_ALREADY_EXIST;
//            }
//        }
//        return new DbConstraintViolationException(errorMsg, e);
//    }

}
