package com.practice.application.exceptions.services;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;

import com.practice.application.employee.EntityNotFoundException;
import com.practice.application.employee.NoResultException;
import com.practice.domain.persistence.entities.Employee;
import com.practice.domain.persistence.entities.RateAlert;
import com.practice.domain.employee.EmployeeRepository;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.domain.persistence.specs.EmployeeSpecification;
import com.practice.application.employee.EmployeeServiceImpl;
import com.practice.application.shared.ServiceErrorHandler;
import com.practice.interfaces.rest.assemblers.DtoAssembler;
import com.practice.interfaces.rest.assemblers.EntityAssembler;
import com.practice.interfaces.rest.web.dtos.NewEmployeeDto;
import com.practice.interfaces.rest.web.dtos.RateAlertDto;
import com.practice.interfaces.rest.web.dtos.SavedEmployeeDto;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private static EmployeeSpecification employeeSpecification;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EntityAssembler entityTransformer;

    @Mock
    private DtoAssembler dtoTransformer;

    @Mock
    private RateAlertRepository rateAlertRepository;

    @Mock
    private ServiceErrorHandler serviceErrorHandler;

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
                    String fieldCriteria,
                    Optional<Employee> expectedResult) {
        Specification<Employee> specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);
        when(dtoTransformer.toDto(any(Employee.class), any(Class.class)))
            .thenReturn(new SavedEmployeeDto());

        SavedEmployeeDto actualResult = employeeService.getEmployeeByFieldCriteria(fieldCriteria);

        assertNotNull(actualResult);
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenDataFound() {
        Supplier<Specification<Employee>> idSpecCall =
            () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<Employee>> textualSpecCall =
            () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        Optional<Employee> filledOptional = Optional.of(new Employee());

        return Stream.of(
            Arguments.of(idSpecCall, "id:1", filledOptional),
            Arguments.of(textualSpecCall, "username:personusername", filledOptional),
            Arguments.of(textualSpecCall, "email:person@test.com", filledOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenDataNotFound")
    public void testGetEmployeeByFieldCriteria_WhenDataNotFound_ThenThrowEntityNotFoundException(
                    Supplier<Specification<Employee>> specsCall,
                    String fieldCriteria,
                    Optional<Employee> expectedResult) {
        Specification<Employee> specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenDataNotFound() {
        Supplier<Specification<Employee>> idSpecCall =
            () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<Employee>> textualSpecCall =
            () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        Optional<Employee> emptyOptional = Optional.empty();

        return Stream.of(
            Arguments.of(idSpecCall, "id:404", emptyOptional),
            Arguments.of(textualSpecCall, "username:personusernameX", emptyOptional),
            Arguments.of(textualSpecCall, "email:personX@test.com", emptyOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenUsingInvalidCriteria")
    public void testGetEmployeeByFieldCriteria_WhenUsingInvalidCriteria_ThenThrowIllegalArgumentException(String fieldCriteria) {
        assertThrows(IllegalArgumentException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenUsingInvalidCriteria() {
        return Stream.of(
            Arguments.of("id404"),
            Arguments.of("id=404"),
            Arguments.of("usernamepersonusernameX"),
            Arguments.of("username=personusernameX"),
            Arguments.of("emailpersonX@test.com"),
            Arguments.of("email=personX@test.com")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaWhenUsingUnsupportedCriteria")
    public void testGetEmployeeByFieldCriteria_WhenUsingUnsupportedCriteria_ThenThrowUnsupportedOperationException(String fieldCriteria) {
        assertThrows(UnsupportedOperationException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaWhenUsingUnsupportedCriteria() {
        return Stream.of(
            Arguments.of("phone_number:061123456"),
            Arguments.of("age:35")
        );
    }

    @Test
    public void testCreateEmployee_WhenEmployeePayloadIsValid_ThenCreateIt() {
        long expectedIdOfNewlyCreatedEmployee = 1;
        Employee entity = mock(Employee.class);
        NewEmployeeDto dto = mock(NewEmployeeDto.class);
        when(entityTransformer.toEntity(any(NewEmployeeDto.class), any(Class.class)))
            .thenReturn(entity);
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(entity);
        when(entity.getId())
            .thenReturn(expectedIdOfNewlyCreatedEmployee);

        long actualIdOfNewlyCreatedEmployee = employeeService.createEmployee(dto);

        assertEquals(expectedIdOfNewlyCreatedEmployee, actualIdOfNewlyCreatedEmployee);
    }

    @ParameterizedTest
    @CsvSource({ "DB constraint is violated for this field: username",
                    "DB constraint is violated for this field: email",
                    "DB constraint is violated for this field: phone number" })
    public void testCreateEmployee_WhenUniqueConstraintViolated_ThenThrowIllegalArgumentException(String errorMsg) {
        Employee entity = mock(Employee.class);
        NewEmployeeDto dto = mock(NewEmployeeDto.class);
        when(entityTransformer.toEntity(any(NewEmployeeDto.class), any(Class.class)))
            .thenReturn(entity);
        when(employeeRepository.save(any(Employee.class)))
            .thenThrow(DataIntegrityViolationException.class);
        when(serviceErrorHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(errorMsg));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.createEmployee(dto));
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsNotDuplicated_ThenUpdateIt() {
        long id = 1;
        Employee entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.save(entity))
            .thenReturn(entity);

        employeeService.updateEmployeeEmailById(id, "new.email@test.com");

        verify(employeeRepository).getOne(id);
        verify(employeeRepository).save(entity);
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsDuplicated_ThenThrowIllegalArgumentException() {
        Employee entity = mock(Employee.class);
        when(employeeRepository.getOne(any(Long.class)))
            .thenReturn(entity);
        when(employeeRepository.save(any(Employee.class)))
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


    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsNew_ThenCreateIt() {
        RateAlert entity = mock(RateAlert.class);
        when(entityTransformer.toEntity(any(RateAlertDto.class), any(Class.class)))
            .thenReturn(entity);
        when(rateAlertRepository.save(any(RateAlert.class)))
            .thenReturn(entity);

        employeeService.registerForScheduledMailAlert(new RateAlertDto());

        verify(entityTransformer).toEntity(any(RateAlertDto.class), any(Class.class));
        verify(rateAlertRepository).save(any(RateAlert.class));
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsDuplicated_ThenThrowIllegalArgumentException() {
        RateAlert entity = mock(RateAlert.class);
        when(entityTransformer.toEntity(any(RateAlertDto.class), any(Class.class)))
            .thenReturn(entity);
        doThrow(DataIntegrityViolationException.class)
            .when(rateAlertRepository).save(any(RateAlert.class));
        when(serviceErrorHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException("DB constraint is violated for this field: email"));

        assertThrows(IllegalArgumentException.class,
            () -> employeeService.registerForScheduledMailAlert(new RateAlertDto()));
    }

}
