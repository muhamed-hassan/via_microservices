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

public class EntityTransformerUT {

    private static EntityTransformer entityTransformer;
    private static EmployeeDto employeeDto;
    private static RateAlertDto rateAlertDTO;
    private static final int AGE = 30;

    @BeforeAll
    public static void init() {
        entityTransformer = new EntityTransformer();

        employeeDto = new EmployeeDto();
        employeeDto.setName("name");
        employeeDto.setUsername("username");
        employeeDto.setPhoneNumber("061123456");
        employeeDto.setEmail("email@test.com");
        employeeDto.setAge(AGE);

        rateAlertDTO = new RateAlertDto();
        rateAlertDTO.setBase("HUF");
        rateAlertDTO.setEmail("email2@test.com");
    }

    @Test
    public void testToEntity_WhenUsingValidEmployeeDto_ThenReturnTransformedEmployeeEntity() {
        EmployeeEntity entity = entityTransformer.toEntity(employeeDto, EmployeeEntity.class);

        assertEquals(entity.getId(), employeeDto.getId());
        assertEquals(0, entity.getId());
        assertEquals(entity.getName(), employeeDto.getName());
        assertEquals(entity.getUsername(), employeeDto.getUsername());
        assertEquals(entity.getPhoneNumber(), employeeDto.getPhoneNumber());
        assertEquals(entity.getEmail(), employeeDto.getEmail());
        assertEquals(entity.getAge(), employeeDto.getAge());
    }

    @Test
    public void testToEntity_WhenUsingValidRateAlertDto_ThenReturnTransformedRateAlertEntity() {
        RateAlertEntity entity = entityTransformer.toEntity(rateAlertDTO, RateAlertEntity.class);

        assertEquals(entity.getBase(), rateAlertDTO.getBase());
        assertEquals(entity.getEmail(), rateAlertDTO.getEmail());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestToEntityToThrowRuntimeException")
    public <DTO, E> void testToEntity_WhenUsingInvalidArgs_ThenThrowRuntimeException(DTO dto, Class<E> entityClass) {
        assertThrows(RuntimeException.class, () -> entityTransformer.toEntity(dto, entityClass));
    }

    private static Stream<Arguments> provideArgsForTestToEntityToThrowRuntimeException() {
        return Stream.of(
            Arguments.of(null, EmployeeEntity.class),
            Arguments.of(employeeDto, null),
            Arguments.of(null, RateAlertEntity.class),
            Arguments.of(rateAlertDTO, null),
            Arguments.of(null, null)
        );
    }

}
