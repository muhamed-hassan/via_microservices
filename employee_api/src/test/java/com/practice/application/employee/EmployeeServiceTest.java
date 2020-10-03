package com.practice.application.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.practice.application.shared.ServiceErrorHandler;
import com.practice.domain.employee.Employee;
import com.practice.domain.employee.EmployeeRepository;
import com.practice.domain.employee.EmployeeSpecification;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static EmployeeSpecification employeeSpecification;

    private EmployeeService employeeService;

    private EmployeeRepository employeeRepository;

    private ServiceErrorHandler serviceErrorHandler;

    @BeforeEach
    void injectRefs() {
        employeeRepository = mock(EmployeeRepository.class);
        employeeSpecification = mock(EmployeeSpecification.class);
        serviceErrorHandler = mock(ServiceErrorHandler.class);
        employeeService = new EmployeeServiceImpl(employeeRepository, employeeSpecification, serviceErrorHandler);
    }

    @Test
    public void testGetEmployees_WhenDataFound_ThenReturnThem() {
        List<SavedEmployeeDto> expectedResult = List.of(new SavedEmployeeDto());
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(expectedResult);

        List<SavedEmployeeDto> actualResult = employeeService.getEmployees();

        assertTrue(actualResult.size() == expectedResult.size());
    }

    @Test
    public void testGetEmployees_WhenDataNotFound_ThenThrowNoResultException() {
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(List.of());

        assertThrows(NoResultException.class,
            () -> employeeService.getEmployees());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenDataFound")
    public void testGetEmployeeByFieldCriteria_WhenDataFound_ThenReturnIt(
                    Supplier<Specification<Employee>> specsCall,
                    String fieldName,
                    String fieldValue,
                    Optional<Employee> expectedResult) {
        Specification<Employee> specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        Employee actualResult = employeeService.getEmployeeByFieldCriteria(fieldName, fieldValue);

        assertNotNull(actualResult);
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenDataFound() {
        Supplier<Specification<Employee>> idSpecCall =
            () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<Employee>> textualSpecCall =
            () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        Optional<Employee> filledOptional = Optional.of(new Employee());

        return Stream.of(
            Arguments.of(idSpecCall, "id", "1", filledOptional),
            Arguments.of(textualSpecCall, "username", "personusername", filledOptional),
            Arguments.of(textualSpecCall, "email", "person@test.com", filledOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenDataNotFound")
    public void testGetEmployeeByFieldCriteria_WhenDataNotFound_ThenThrowEntityNotFoundException(
                    Supplier<Specification<Employee>> specsCall,
                    String fieldName,
                    String fieldValue,
                    Optional<Employee> expectedResult) {
        Specification<Employee> specification = mock(Specification.class);
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

        Optional<Employee> emptyOptional = Optional.empty();

        return Stream.of(
            Arguments.of(idSpecCall, "id", "404", emptyOptional),
            Arguments.of(textualSpecCall, "username", "personusernameX", emptyOptional),
            Arguments.of(textualSpecCall, "email", "personX@test.com", emptyOptional)
        );
    }

    @Test
    public void testCreateEmployee_WhenEmployeePayloadIsValid_ThenCreateIt() {
        long expectedIdOfNewlyCreatedEmployee = 1;
        Employee entity = mock(Employee.class);
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(entity);
        when(entity.getId())
            .thenReturn(expectedIdOfNewlyCreatedEmployee);

        long actualIdOfNewlyCreatedEmployee = employeeService.createEmployee(entity);

        assertEquals(expectedIdOfNewlyCreatedEmployee, actualIdOfNewlyCreatedEmployee);
    }

    @ParameterizedTest
    @CsvSource({ "DB constraint is violated for this field: username",
                    "DB constraint is violated for this field: email",
                    "DB constraint is violated for this field: phone number" })
    public void testCreateEmployee_WhenUniqueConstraintViolated_ThenThrowIllegalArgumentException(String errorMsg) {
        Employee entity = mock(Employee.class);
        when(employeeRepository.save(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceErrorHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(errorMsg));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.createEmployee(entity));
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsNotDuplicated_ThenUpdateIt() {
        long id = 1;
        Employee entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.saveAndFlush(entity))
            .thenReturn(entity);

        employeeService.updateEmployeeEmailById(id, "new.email@test.com");

        verify(employeeRepository).getOne(id);
        verify(employeeRepository).saveAndFlush(entity);
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsDuplicated_ThenThrowIllegalArgumentException() {
        Employee entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.saveAndFlush(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceErrorHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException("DB constraint is violated for this field: email"));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.updateEmployeeEmailById(1, "new.email@test.com"));
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeNotExist_ThenThrowEntityNotFoundException() {
        when(employeeRepository.getOne(any(Long.class)))
            .thenThrow(javax.persistence.EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.updateEmployeeEmailById(1, "new.email@test.com"));
    }

    @Test
    public void testDeleteEmployeeEmailById_WhenEmployeeExist_ThenDeleteIt() {
        doNothing()
            .when(employeeRepository).deleteById(anyLong());

        employeeService.deleteEmployeeById(1);

        verify(employeeRepository).deleteById(anyLong());
    }

    @Test
    public void testDeleteEmployeeEmailById_WhenEmployeeNotExist_ThenThrowEntityNotFoundException() {
        doThrow(EmptyResultDataAccessException.class)
            .when(employeeRepository).deleteById(anyLong());

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.deleteEmployeeById(1));
    }

}
