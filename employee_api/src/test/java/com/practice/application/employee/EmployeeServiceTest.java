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
import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static EmployeeSpecification employeeSpecification;

    private EmployeeService employeeService;

    private EmployeeRepository employeeRepository;

    private RateAlertRepository rateAlertRepository;

    private ServiceExceptionHandler serviceExceptionHandler;

    @BeforeEach
    void injectRefs() {
        employeeRepository = mock(EmployeeRepository.class);
        rateAlertRepository = mock(RateAlertRepository.class);
        employeeSpecification = mock(EmployeeSpecification.class);
        serviceExceptionHandler = mock(ServiceExceptionHandler.class);
        employeeService = new EmployeeServiceImpl(employeeRepository, employeeSpecification, rateAlertRepository, serviceExceptionHandler);
    }

    @Test
    void shouldGetEmployeesWhenDataFound() {
        var expectedResult = List.of(new SavedEmployeeDto());
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(expectedResult);

        var actualResult = employeeService.getEmployees();

        assertEquals(expectedResult.size(), actualResult.size());
    }

    @Test
    void shouldThrowNoResultExceptionWhenCallGetEmployeesAndDataNotFound() {
        when(employeeRepository.findAllSavedEmployees())
            .thenReturn(List.of());

        assertThrows(NoResultException.class,
            () -> employeeService.getEmployees());
    }

    @ParameterizedTest
    @MethodSource("provideArgsWhenEmployeeQueriedByFieldCriteriaAndDataFound")
    void shouldGetEmployeeWhenQueriedByFieldCriteriaAndDataFound(
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

    private static Stream<Arguments> provideArgsWhenEmployeeQueriedByFieldCriteriaAndDataFound() {
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
    @MethodSource("provideArgsWhenEmployeeQueriedByFieldCriteriaAndDataNotFound")
    void shouldThrowEntityNotFoundExceptionWhenQueriedByFieldCriteriaAndDataNotFound(
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

    private static Stream<Arguments> provideArgsWhenEmployeeQueriedByFieldCriteriaAndDataNotFound() {
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
    void shouldCreateEmployeeWhenPayloadIsValid() {
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
    void shouldFailCreateEmployeeAndThrowIllegalArgumentExceptionWhenUniqueConstraintViolated(String errorMsg) {
        var entity = mock(Employee.class);
        when(employeeRepository.save(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceExceptionHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(errorMsg));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.createEmployee(entity));
    }

    @Test
    void shouldUpdateEmployeeEmailWhenEmployeeExistAndEmailIsNotDuplicated() {
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
    void shouldFailUpdateEmployeeEmailAndThrowIllegalArgumentExceptionWhenEmployeeExistAndEmailIsDuplicated() {
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
    void shouldFailUpdateEmployeeEmailAndThrowEntityNotFoundExceptionWhenEmployeeNotExist() {
        when(employeeRepository.getOne(any(Long.class)))
            .thenThrow(javax.persistence.EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.updateEmployeeEmailById(1L, "new.email@test.com"));
    }

    @Test
    void shouldDeleteEmployeeWhenEmployeeExist() {
        var id = 1L;
        doNothing()
            .when(employeeRepository).deleteById(anyLong());

        employeeService.deleteEmployeeById(id);

        verify(employeeRepository).deleteById(id);
    }

    @Test
    void shouldFailDeleteEmployeeAndThrowEntityNotFoundExceptionWhenEmployeeNotExist() {
        doThrow(EmptyResultDataAccessException.class)
            .when(employeeRepository).deleteById(anyLong());

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.deleteEmployeeById(1L));
    }

    @Test
    void shouldRegisterForScheduledMailAlertWhenEmailIsNew() {
        var entity = mock(RateAlert.class);
        when(rateAlertRepository.save(any(RateAlert.class)))
            .thenReturn(entity);

        employeeService.registerForScheduledMailAlert(new RateAlert());

        verify(rateAlertRepository).save(any(RateAlert.class));
    }

    @Test
    void shouldFailAlertSchedulingAndThrowIllegalArgumentExceptionWhenEmailIsDuplicated() {
        doThrow(DataIntegrityViolationException.class)
            .when(rateAlertRepository).save(any(RateAlert.class));
        when(serviceExceptionHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.registerForScheduledMailAlert(new RateAlert()));
    }

}
