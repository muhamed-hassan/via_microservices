package com.practice.application.employee;

import static com.practice.utils.ErrorKeys.DB_CONSTRAINT_VIOLATED_EMAIL;
import static com.practice.utils.ErrorMsgsCache.getMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;

import com.practice.application.shared.ServiceExceptionHandler;
import com.practice.domain.employee.Employee;
import com.practice.domain.employee.EmployeeRepository;
import com.practice.domain.employee.EmployeeSpecification;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static EmployeeSpecification employeeSpecification;

    private EmployeeService employeeService;

    private EmployeeRepository employeeRepository;

    private ServiceExceptionHandler serviceExceptionHandler;

    @BeforeEach
    void injectRefs() {
        employeeRepository = mock(EmployeeRepository.class);
        employeeSpecification = mock(EmployeeSpecification.class);
        serviceExceptionHandler = mock(ServiceExceptionHandler.class);
        employeeService = new EmployeeServiceImpl(employeeRepository, employeeSpecification, serviceExceptionHandler);
    }

    @Test
    void testGetEmployeesWhenDataFoundThenReturnThem() {
        var expectedResult = List.of(new SavedEmployeeDto());
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(expectedResult);

        var actualResult = employeeService.getEmployees();

        assertEquals(expectedResult.size(), actualResult.size());
    }

    @Test
    void testGetEmployeesWhenDataNotFoundThenThrowNoResultException() {
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(List.of());

        assertThrows(NoResultException.class,
            () -> employeeService.getEmployees());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenDataFound")
    void testGetEmployeeByFieldCriteriaWhenDataFoundThenReturnIt(
                    Supplier<Specification<Employee>> specsCall,
                    String fieldName,
                    String fieldValue,
                    Optional<Employee> expectedResult) {
        var specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        var actualResult = employeeService.getEmployeeByFieldCriteria(fieldName, fieldValue);

        assertNotNull(actualResult);
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenDataFound() {
        Supplier<Specification<Employee>> idSpecCall =
            () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<Employee>> textualSpecCall =
            () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        var filledOptional = Optional.of(new Employee());

        return Stream.of(
            Arguments.of(idSpecCall, "id", "1", filledOptional),
            Arguments.of(textualSpecCall, "username", "personusername", filledOptional),
            Arguments.of(textualSpecCall, "email", "person@test.com", filledOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenDataNotFound")
    void testGetEmployeeByFieldCriteriaWhenDataNotFoundThenThrowEntityNotFoundException(
                    Supplier<Specification<Employee>> specsCall,
                    String fieldName,
                    String fieldValue,
                    Optional<Employee> expectedResult) {
        var specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldName, fieldValue));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenDataNotFound() {
        Supplier<Specification<Employee>> idSpecCall =
            () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<Employee>> textualSpecCall =
            () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        var emptyOptional = Optional.empty();

        return Stream.of(
            Arguments.of(idSpecCall, "id", "404", emptyOptional),
            Arguments.of(textualSpecCall, "username", "personusernameX", emptyOptional),
            Arguments.of(textualSpecCall, "email", "personX@test.com", emptyOptional)
        );
    }

    @Test
    void testCreateEmployeeWhenEmployeePayloadIsValidThenCreateIt() {
        var expectedIdOfNewlyCreatedEmployee = 1L;
        var entity = mock(Employee.class);
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(entity);
        when(entity.getId())
            .thenReturn(expectedIdOfNewlyCreatedEmployee);

        var actualIdOfNewlyCreatedEmployee = employeeService.createEmployee(entity);

        assertEquals(expectedIdOfNewlyCreatedEmployee, actualIdOfNewlyCreatedEmployee);
    }

    @ParameterizedTest
    @CsvSource({ "DB constraint is violated for this field: username",
                    "DB constraint is violated for this field: email",
                    "DB constraint is violated for this field: phone number" })
    void testCreateEmployeeWhenUniqueConstraintViolatedThenThrowIllegalArgumentException(String errorMsg) {
        var entity = mock(Employee.class);
        when(employeeRepository.save(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceExceptionHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(errorMsg));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.createEmployee(entity));
    }

    @Test
    void testUpdateEmployeeEmailByIdWhenEmployeeExistAndEmailIsNotDuplicatedThenUpdateIt() {
        var id = 1L;
        var entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.saveAndFlush(entity))
            .thenReturn(entity);

        employeeService.updateEmployeeEmailById(id, "new.email@test.com");

        verify(employeeRepository).getOne(id);
        verify(employeeRepository).saveAndFlush(entity);
    }

    @Test
    void testUpdateEmployeeEmailByIdWhenEmployeeExistAndEmailIsDuplicatedThenThrowIllegalArgumentException() {
        var entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.saveAndFlush(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceExceptionHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.updateEmployeeEmailById(1L, "new.email@test.com"));
    }

    @Test
    void testUpdateEmployeeEmailByIdWhenEmployeeNotExistThenThrowEntityNotFoundException() {
        when(employeeRepository.getOne(any(Long.class)))
            .thenThrow(javax.persistence.EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.updateEmployeeEmailById(1L, "new.email@test.com"));
    }

    @Test
    void testDeleteEmployeeEmailByIdWhenEmployeeExistThenDeleteIt() {
        var id = 1L;
        doNothing()
            .when(employeeRepository).deleteById(anyLong());

        employeeService.deleteEmployeeById(id);

        verify(employeeRepository).deleteById(id);
    }

    @Test
    void testDeleteEmployeeEmailByIdWhenEmployeeNotExistThenThrowEntityNotFoundException() {
        doThrow(EmptyResultDataAccessException.class)
            .when(employeeRepository).deleteById(anyLong());

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.deleteEmployeeById(1L));
    }

}
