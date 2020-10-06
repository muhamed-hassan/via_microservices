package com.practice.it;

import static com.practice.utils.Mappings.ALL_EMPLOYEES_JSON;
import static com.practice.utils.Mappings.DUPLICATED_EMAIL_JSON;
import static com.practice.utils.Mappings.DUPLICATED_PHONE_NUMBER_JSON;
import static com.practice.utils.Mappings.DUPLICATED_USERNAME_JSON;
import static com.practice.utils.Mappings.EMPLOYEE_WITH_CRITERIA_JSON;
import static com.practice.utils.Mappings.ENTITY_NOT_FOUND_JSON;
import static com.practice.utils.Mappings.INVALID_EMAIL_JSON;
import static com.practice.utils.Mappings.INVALID_FIELD_CRITERIA_JSON;
import static com.practice.utils.Mappings.INVALID_MAX_AGE_JSON;
import static com.practice.utils.Mappings.INVALID_MIN_AGE_JSON;
import static com.practice.utils.Mappings.INVALID_NAME_JSON;
import static com.practice.utils.Mappings.INVALID_PHONE_NUMBER_JSON;
import static com.practice.utils.Mappings.INVALID_USERNAME_JSON;
import static com.practice.utils.Mappings.NEW_EMAIL_JSON;
import static com.practice.utils.Mappings.NEW_EMAIL_WITH_DUPLICATED_VALUE_JSON;
import static com.practice.utils.Mappings.NEW_EMAIL_WITH_INVALID_VALUE_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_DUPLICATED_EMAIL_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_DUPLICATED_PHONE_NUMBER_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_DUPLICATED_USERNAME_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_EMAIL_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_MAX_AGE_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_MIN_AGE_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_NAME_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_PHONE_NUMBER_JSON;
import static com.practice.utils.Mappings.NEW_EMPLOYEE_WITH_INVALID_USERNAME_JSON;
import static com.practice.utils.Mappings.NO_DATA_FOUND_JSON;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

class EmployeeControllerIT extends BaseControllerIT {

    @Sql(scripts = "classpath:db/scripts/all_employees_data.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testGetEmployees_WhenDataFound_ThenReturn200WithData() 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    get("/v1/employees")
                                                    .accept(MediaType.APPLICATION_JSON));
        
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(getMappingFromInternalApi(ALL_EMPLOYEES_JSON), true));
    }

    @Test
    void testGetEmployees_WhenDataNotFound_ThenReturn404WithErrorMsg() 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    get("/v1/employees")
                                                    .accept(MediaType.APPLICATION_JSON));
        
        resultActions.andExpect(status().isNotFound())
                        .andExpect(content().json(getMappingFromInternalApi(NO_DATA_FOUND_JSON), true));
    }

    @Sql(scripts = "classpath:db/scripts/employee_with_criteria.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataFound")
    void testGetEmployeeByFieldCriteria_WhenDataFound_ThenReturn200WithData(String pathVariable) 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                    .accept(MediaType.APPLICATION_JSON));
        
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(getMappingFromInternalApi(EMPLOYEE_WITH_CRITERIA_JSON), true));
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataFound() {
        return Stream.of(
            Arguments.of("id:1001"),
            Arguments.of("email:wanya@test.com"),
            Arguments.of("username:wanya_costrau")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataNotFound")
    void testGetEmployeeByFieldCriteria_WhenDataNotFound_ThenReturn404WithErrorMsg(String pathVariable) 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                        .andExpect(content().json(getMappingFromInternalApi(ENTITY_NOT_FOUND_JSON), true));
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataNotFound() {
        return Stream.of(
            Arguments.of("id:404"),
            Arguments.of("email:anna@test.com"),
            Arguments.of("username:anna_nyporka")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenSendingInvalidCriteria")
    void testGetEmployeeByFieldCriteria_WhenSendingInvalidCriteria_ThenReturn400WithErrorMsg(String pathVariable) 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(INVALID_FIELD_CRITERIA_JSON), true));
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteriaWhenSendingInvalidCriteria() {
        return Stream.of(
            Arguments.of("id=2"),
            Arguments.of("id2"),
            Arguments.of("idx:2"),
            Arguments.of("email=anna@test.com"),
            Arguments.of("emailanna@test.com"),
            Arguments.of("emailx:anna@test.com"),
            Arguments.of("username=anna_nyporka"),
            Arguments.of("usernameanna_nyporka"),
            Arguments.of("usernamex:anna_nyporka")
        );
    }

    @Test
    void testCreateEmployee_WhenPayloadIsValid_ThenSaveItAndReturn201WithItsLocation()
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    post("/v1/employees")
                                                    .content(getMappingFromInternalApi(NEW_EMPLOYEE_JSON))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isCreated())
                        .andExpect(header().exists("Location"));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestCreateEmployeeWhenPayloadIsInvalid")
    void testCreateEmployee_WhenPayloadIsInvalid_ThenReturn400WithErrorMsg(String requestBodyFile, String errorMsgFile)
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    post("/v1/employees")
                                                    .content(getMappingFromInternalApi(requestBodyFile))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(errorMsgFile), true));
    }

    private static Stream<Arguments> provideArgumentsForTestCreateEmployeeWhenPayloadIsInvalid() {
        return Stream.of(
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_EMAIL_JSON, INVALID_EMAIL_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_MAX_AGE_JSON, INVALID_MAX_AGE_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_MIN_AGE_JSON, INVALID_MIN_AGE_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_NAME_JSON, INVALID_NAME_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_PHONE_NUMBER_JSON, INVALID_PHONE_NUMBER_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_USERNAME_JSON, INVALID_USERNAME_JSON)
        );
    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @ParameterizedTest
    @MethodSource("provideArgumentsForTestCreateEmployeeWhenDbConstraintIsViolated")
    void testCreateEmployee_WhenDbConstraintIsViolated_ThenReturn400WithErrorMsg(String requestBodyFile, String errorMsgFile)
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    post("/v1/employees")
                                                    .content(getMappingFromInternalApi(requestBodyFile))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(errorMsgFile), true));
    }

    private static Stream<Arguments> provideArgumentsForTestCreateEmployeeWhenDbConstraintIsViolated() {
        return Stream.of(
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_EMAIL_JSON, DUPLICATED_EMAIL_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_PHONE_NUMBER_JSON, DUPLICATED_PHONE_NUMBER_JSON),
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_USERNAME_JSON, DUPLICATED_USERNAME_JSON)
        );
    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testUpdateEmployeeEmailById_WhenEmailIsValidAndNotDuplicated_ThenUpdateAndReturn204()
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    patch(String.format("/v1/employees/%d", 1001))
                                                    .content(getMappingFromInternalApi(NEW_EMAIL_JSON))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
    }

    @Sql(scripts = "classpath:db/scripts/all_employees_data.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testUpdateEmployeeEmailById_WhenEmailIsValidAndDuplicated_ThenReturn400WithErrorMsg()
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    patch(String.format("/v1/employees/%d", 1001))
                                                    .content(getMappingFromInternalApi(NEW_EMAIL_WITH_DUPLICATED_VALUE_JSON))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(DUPLICATED_EMAIL_JSON), true));

    }

    @Test
    void testUpdateEmployeeEmailById_WhenEmailIsValidAndEmployeeNotFound_ThenReturn404WithErrorMsg()
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    patch(String.format("/v1/employees/%d", 404))
                                                    .content(getMappingFromInternalApi(NEW_EMAIL_JSON))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                        .andExpect(content().json(getMappingFromInternalApi(ENTITY_NOT_FOUND_JSON), true));
    }

    @Test
    void testUpdateEmployeeEmailById_WhenEmailIsInvalid_ThenReturn400WithErrorMsg() 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    patch(String.format("/v1/employees/%d", 1001))
                                                    .content(getMappingFromInternalApi(NEW_EMAIL_WITH_INVALID_VALUE_JSON))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(INVALID_EMAIL_JSON), true));

    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testDeleteEmployeeById_WhenDataFound_ThenDeleteAndReturn204() 
            throws Exception {
        
        ResultActions resultActions = getMockMvc().perform(
                                                    delete(String.format("/v1/employees/%d", 1001))
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEmployeeById_WhenDataNotFound_ThenReturn404WithErrorMsg() 
            throws Exception {

        ResultActions resultActions = getMockMvc().perform(
                                                    delete(String.format("/v1/employees/%d", 404))
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                        .andExpect(content().json(getMappingFromInternalApi(ENTITY_NOT_FOUND_JSON), true));
    }

}
