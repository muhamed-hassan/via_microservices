package com.practice.services;

import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER;
import static com.practice.persistence.entities.EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME;
import static com.practice.configs.constants.Messages.EMAIL_ALREADY_EXIST;
import static com.practice.configs.constants.Messages.PHONE_NUMBER_ALREADY_EXIST;
import static com.practice.configs.constants.Messages.USER_NAME_ALREADY_EXIST;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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

import com.practice.exceptions.DbConstraintViolationException;
import com.practice.exceptions.EntityModificationException;
import com.practice.exceptions.EntityNotFoundException;
import com.practice.exceptions.handlers.EmployeeServiceErrorHandler;
import com.practice.persistence.entities.EmployeeEntity;
import com.practice.persistence.repositories.EmployeeRepository;
import com.practice.persistence.specs.EmployeeSpecification;
import com.practice.services.impl.EmployeeServiceImpl;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.EmployeeDto;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceUT {

    @Mock
    private static EmployeeSpecification employeeSpecification;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EntityTransformer entityTransformer;

    @Mock
    private EmployeeServiceErrorHandler employeeServiceErrorHandler;

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployees")
    public void testGetEmployees_WhenProvidingNoSelectionCriteria_ThenReturnCurrentEmployees(List<EmployeeEntity> employeesArg, int expectedSize) {
        when(employeeRepository.findAll())
            .thenReturn(employeesArg);

        List<EmployeeEntity> employees = employeeService.getEmployees();

        assertTrue(employees.size() == expectedSize);
        verify(employeeRepository, times(1)).findAll();
    }

    private static Stream<Arguments> provideArgsForTestGetEmployees() {
        List<EmployeeEntity> fullListOfEmployees = List.of(new EmployeeEntity());
        List<EmployeeEntity> emptyListOfEmployees = List.of();
        return Stream.of(
            Arguments.of(fullListOfEmployees, fullListOfEmployees.size()),
            Arguments.of(emptyListOfEmployees, emptyListOfEmployees.size())
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaToGetResult")
    public void testGetEmployeeByFieldCriteria_WhenQueryingByFieldAndEmployeeExist_ThenReturnIt(
                    Supplier<Specification<EmployeeEntity>> specsCall,
                    String fieldCriteria,
                    Optional<EmployeeEntity> expectedResult) {
        Specification<EmployeeEntity> specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        EmployeeEntity entity = employeeService.getEmployeeByFieldCriteria(fieldCriteria);

        assertNotNull(entity != null);
        verify(employeeRepository, times(1)).findOne(any(Specification.class));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaToGetResult() {
        Supplier<Specification<EmployeeEntity>> idSpecCall = () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<EmployeeEntity>> textualSpecCall = () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        Optional<EmployeeEntity> filledOptional = Optional.of(new EmployeeEntity());

        return Stream.of(
            Arguments.of(idSpecCall, "id:1", filledOptional),
            Arguments.of(textualSpecCall, "username:personusername", filledOptional),
            Arguments.of(textualSpecCall, "email:person@test.com", filledOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaToThrowEntityNotFoundException")
    public void testGetEmployeeByFieldCriteria_WhenQueryingByFieldAndEmployeeDoesNotExist_ThenThrowEntityNotFoundException(
                    Supplier<Specification<EmployeeEntity>> specsCall,
                    String fieldCriteria,
                    Optional<EmployeeEntity> expectedResult) {
        Specification<EmployeeEntity> specification = mock(Specification.class);
        when(specsCall.get())
            .thenReturn(specification);
        when(employeeRepository.findOne(any(Specification.class)))
            .thenReturn(expectedResult);

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaToThrowEntityNotFoundException() {
        Supplier<Specification<EmployeeEntity>> idSpecCall = () -> employeeSpecification.getEmployeeByIdSpec(anyString(), anyLong());
        Supplier<Specification<EmployeeEntity>> textualSpecCall = () -> employeeSpecification.getEmployeeByTextualSpec(anyString(), anyString());

        Optional<EmployeeEntity> emptyOptional = Optional.empty();

        return Stream.of(
            Arguments.of(idSpecCall, "id:404", emptyOptional),
            Arguments.of(textualSpecCall, "username:personusernameX", emptyOptional),
            Arguments.of(textualSpecCall, "email:personX@test.com", emptyOptional)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaToThrowIllegalArgumentException")
    public void testGetEmployeeByFieldCriteria_WhenQueryingByFieldAndUsingInvalidCriteriaFormat_ThenThrowIllegalArgumentException(String fieldCriteria) {
        assertThrows(IllegalArgumentException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaToThrowIllegalArgumentException() {
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
    @MethodSource("provideArgsForTestGetEmployeeByFieldCriteriaToThrowUnsupportedOperationException")
    public void testGetEmployeeByFieldCriteria_WhenQueryingByFieldAndUsingUnsupportedCriterion_ThenThrowUnsupportedOperationException(String fieldCriteria) {
        assertThrows(UnsupportedOperationException.class,
            () -> employeeService.getEmployeeByFieldCriteria(fieldCriteria));
    }

    private static Stream<Arguments> provideArgsForTestGetEmployeeByFieldCriteriaToThrowUnsupportedOperationException() {
        return Stream.of(
            Arguments.of("phone_number:061123456"),
            Arguments.of("age:35")
        );
    }

    @Test
    public void testCreateEmployee_WhenEmployeePayloadIsValid_ThenCreateIt() {
        long expectedIdOfNewlyCreatedEmployee = 1;
        EmployeeEntity entity = mock(EmployeeEntity.class);
        EmployeeDto dto = mock(EmployeeDto.class);
        when(entityTransformer.toEntity(any(EmployeeDto.class), any(Class.class)))
            .thenReturn(entity);
        when(employeeRepository.save(any(EmployeeEntity.class)))
            .thenReturn(entity);
        when(entity.getId())
            .thenReturn(expectedIdOfNewlyCreatedEmployee);

        long actualIdOfNewlyCreatedEmployee = employeeService.createEmployee(dto);

        assertEquals(expectedIdOfNewlyCreatedEmployee, actualIdOfNewlyCreatedEmployee);
        verify(entityTransformer, times(1)).toEntity(any(EmployeeDto.class), any(Class.class));
        verify(employeeRepository, times(1)).save(any(EmployeeEntity.class));
    }

    @ParameterizedTest
    @CsvSource({ "..." + EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME + "..." + "," + USER_NAME_ALREADY_EXIST,
        "..." + EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL + "..." + "," + EMAIL_ALREADY_EXIST,
        "..." + EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER + "..." + "," + PHONE_NUMBER_ALREADY_EXIST })
    public void testCreateEmployee_WhenUniqueConstraintViolated_ThenThrowDbConstraintViolationException(String violatedConstraint, String errorMsg) {
        EmployeeEntity entity = mock(EmployeeEntity.class);
        EmployeeDto dto = mock(EmployeeDto.class);
        when(entityTransformer.toEntity(any(EmployeeDto.class), any(Class.class)))
            .thenReturn(entity);
        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(violatedConstraint);
        when(employeeRepository.save(any(EmployeeEntity.class)))
            .thenThrow(dataIntegrityViolationException);
        DbConstraintViolationException dbConstraintViolationException = new DbConstraintViolationException(errorMsg, dataIntegrityViolationException);
        doThrow(dbConstraintViolationException)
            .when(employeeServiceErrorHandler).handleDataIntegrityViolationException(dataIntegrityViolationException);

        DbConstraintViolationException thrown = assertThrows(DbConstraintViolationException.class, () -> employeeService.createEmployee(dto));
        assertTrue(thrown.getMessage().equals(errorMsg));
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsNotDuplicated_ThenUpdateIt() {
        int affectedRows = 1;
        when(employeeRepository.updateEmail(anyLong(), anyString()))
            .thenReturn(affectedRows);

        employeeService.updateEmployeeEmailById(1L, "new.email@test.com");

        verify(employeeRepository, times(1)).updateEmail(anyLong(), anyString());
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmployeeExistAndEmailIsDuplicated_ThenThrowDbConstraintViolationException() {
        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException("..." + EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL + "...");
        when(employeeRepository.updateEmail(anyLong(), anyString()))
            .thenThrow(dataIntegrityViolationException);
        DbConstraintViolationException dbConstraintViolationException = new DbConstraintViolationException(EMAIL_ALREADY_EXIST, dataIntegrityViolationException);
        doThrow(dbConstraintViolationException)
            .when(employeeServiceErrorHandler).handleDataIntegrityViolationException(dataIntegrityViolationException);

        Executable executable = () -> employeeService.updateEmployeeEmailById(1L, "new.email@test.com");
        DbConstraintViolationException thrown = assertThrows(DbConstraintViolationException.class, executable);
        assertTrue(thrown.getMessage().equals(EMAIL_ALREADY_EXIST));
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenFailedToUpdate_ThenThrowEntityModificationException() {
        int affectedRows = 0;
        when(employeeRepository.updateEmail(anyLong(), anyString()))
            .thenReturn(affectedRows);

        assertThrows(EntityModificationException.class,
            () -> employeeService.updateEmployeeEmailById(1L, "new.email@test.com"));
    }

    @Test
    public void testDeleteEmployeeEmailById_WhenEmployeeExist_ThenDeleteIt() {
        doNothing()
            .when(employeeRepository).deleteById(anyLong());

        employeeService.deleteEmployeeById(1L);

        verify(employeeRepository, times(1)).deleteById(anyLong());
    }

    @Test
    public void testDeleteEmployeeEmailById_WhenEmployeeNotExist_ThenThrowEntityNotFoundException() {
        doThrow(EmptyResultDataAccessException.class)
            .when(employeeRepository).deleteById(anyLong());

        assertThrows(EntityNotFoundException.class,
            () -> employeeService.deleteEmployeeById(1L));
    }

}
