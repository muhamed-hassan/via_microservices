package com.practice.transfomers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.practice.persistence.entities.EmployeeEntity;
import com.practice.persistence.entities.RateAlertEntity;
import com.practice.web.dtos.EmployeeDto;
import com.practice.web.dtos.RateAlertDto;

public class DtoTransformerUT {

    private static DtoTransformer dtoTransformer;
    private static EmployeeEntity employeeEntity;
    private static RateAlertEntity rateAlertEntity;
    private static final long ID = 1L;
    private static final int AGE = 30;

    @BeforeAll
    public static void init() {
        dtoTransformer = new DtoTransformer();

        employeeEntity = new EmployeeEntity();
        employeeEntity.setId(ID);
        employeeEntity.setName("name");
        employeeEntity.setUsername("username");
        employeeEntity.setPhoneNumber("061123456");
        employeeEntity.setEmail("email@test.com");
        employeeEntity.setAge(AGE);

        rateAlertEntity = new RateAlertEntity();
        rateAlertEntity.setId(ID);
        rateAlertEntity.setBase("HUF");
        rateAlertEntity.setEmail("email2@test.com");
    }

    @Test
    public void testToDto_WhenUsingValidEmployeeEntity_ThenReturnTransformedEmployeeDto() {
        EmployeeDto dto = dtoTransformer.toDto(employeeEntity, EmployeeDto.class);

        assertEquals(dto.getId(), employeeEntity.getId());
        assertEquals(ID, employeeEntity.getId());
        assertEquals(dto.getName(), employeeEntity.getName());
        assertEquals(dto.getUsername(), employeeEntity.getUsername());
        assertEquals(dto.getPhoneNumber(), employeeEntity.getPhoneNumber());
        assertEquals(dto.getEmail(), employeeEntity.getEmail());
        assertEquals(dto.getAge(), employeeEntity.getAge());
    }

    @Test
    public void testToDto_WhenUsingValidRateAlertEntity_ThenReturnTransformedRateAlertDto() {
        RateAlertDto dto = dtoTransformer.toDto(rateAlertEntity, RateAlertDto.class);

        assertEquals(ID, rateAlertEntity.getId());
        assertEquals(dto.getBase(), rateAlertEntity.getBase());
        assertEquals(dto.getEmail(), rateAlertEntity.getEmail());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestToArgToThrowRuntimeException")
    public <E, DTO> void testToDto_WhenUsingInvalidArgs_ThenThrowRuntimeException(E entity, Class<DTO> dtoClass) {
        assertThrows(RuntimeException.class, () -> dtoTransformer.toDto(entity, dtoClass));
    }

    private static Stream<Arguments> provideArgsForTestToArgToThrowRuntimeException() {
        return Stream.of(
            Arguments.of(null, EmployeeDto.class),
            Arguments.of(employeeEntity, null),
            Arguments.of(null, RateAlertDto.class),
            Arguments.of(rateAlertEntity, null),
            Arguments.of(null, null)
        );
    }

}
