package com.practice.it;

import static com.practice.utils.ErrorKeys.DB_CONSTRAINT_VIOLATED_EMAIL;
import static com.practice.utils.ErrorKeys.DB_CONSTRAINT_VIOLATED_PHONE_NUMBER;
import static com.practice.utils.ErrorKeys.DB_CONSTRAINT_VIOLATED_USERNAME;
import static com.practice.utils.ErrorKeys.ENTITY_NOT_FOUND;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_CRITERIA;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_CURRENCY_CODE;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_EMAIL;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_MAX_AGE;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_MIN_AGE;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_NAME;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_PHONE_NUMBER;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_USERNAME;
import static com.practice.utils.ErrorKeys.NO_DATA_FOUND;
import static com.practice.utils.ErrorMsgsCache.getMessage;
import static com.practice.utils.Mappings.ALL_EMPLOYEES_JSON;
import static com.practice.utils.Mappings.EMPLOYEE_WITH_CRITERIA_JSON;
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
import static com.practice.utils.Mappings.NEW_RATE_ALERT_JSON;
import static com.practice.utils.Mappings.NEW_RATE_ALERT_WITH_INVALID_BASE_JSON;
import static com.practice.utils.Mappings.NEW_RATE_ALERT_WITH_INVALID_EMAIL_JSON;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

class EmployeeControllerIT extends BaseControllerIT {

    @Sql(scripts = "classpath:db/scripts/all_employees_data.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldReturnStatus200WithDataWhenRequestAllEmployeesAndDataFound()
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        get("/v1/employees")
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isOk(), content().json(getMappingFromInternalApi(ALL_EMPLOYEES_JSON), true));
    }

    @Test
    void shouldReturnStatus404WithErrMsgWhenRequestAllEmployeesAndDataNotFound()
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        get("/v1/employees")
                                                            .accept(MediaType.APPLICATION_JSON));
        
        expect(resultActions, status().isNotFound(), jsonPath("$.error").value(getMessage(NO_DATA_FOUND)));
    }

    @Sql(scripts = "classpath:db/scripts/employee_with_criteria.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @ParameterizedTest
    @MethodSource("provideArgsWhenRequestEmployeeByFieldCriteriaAndDataFound")
    void shouldReturnStatus200WithDataWhenRequestEmployeeByFieldCriteriaAndDataFound(String pathVariable)
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isOk(), content().json(getMappingFromInternalApi(EMPLOYEE_WITH_CRITERIA_JSON), true));
    }

    private static Stream<Arguments> provideArgsWhenRequestEmployeeByFieldCriteriaAndDataFound() {
        return Stream.of(
            Arguments.of("id:1001"),
            Arguments.of("email:wanya@test.com"),
            Arguments.of("username:wanya_costrau")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsWhenRequestEmployeeByFieldCriteriaAndDataNotFound")
    void shouldReturnStatus404WithErrMsgWhenRequestEmployeeByFieldCriteriaAndDataNotFound(String pathVariable)
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isNotFound(), jsonPath("$.error").value(getMessage(ENTITY_NOT_FOUND)));
    }

    private static Stream<Arguments> provideArgsWhenRequestEmployeeByFieldCriteriaAndDataNotFound() {
        return Stream.of(
            Arguments.of("id:404"),
            Arguments.of("email:anna@test.com"),
            Arguments.of("username:anna_nyporka")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsWhenRequestEmployeeByFieldCriteriaAndCriteriaIsInvalid")
    void shouldReturnStatus400WithErrMsgWhenRequestEmployeeByFieldCriteriaAndCriteriaIsInvalid(String pathVariable)
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        get(String.format("/v1/employees/criteria/%s", pathVariable))
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(getMessage(INVALID_VALUE_CRITERIA)));
    }

    private static Stream<Arguments> provideArgsWhenRequestEmployeeByFieldCriteriaAndCriteriaIsInvalid() {
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

    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldCreateEmployeeAndReturnStatus201WithItsLocationWhenPayloadIsValid()
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees")
                                                            .content(getMappingFromInternalApi(NEW_EMPLOYEE_JSON))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isCreated(), header().exists("Location"));
    }

    @ParameterizedTest
    @MethodSource("provideArgsWhenPayloadIsInvalidOnEmployeeCreation")
    void shouldFailCreateEmployeeAndReturnStatus400WithErrMsgWhenPayloadIsInvalid(String requestBodyFile, String errorMsg)
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees")
                                                            .content(getMappingFromInternalApi(requestBodyFile))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArgsWhenPayloadIsInvalidOnEmployeeCreation() {
        return Stream.of(
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_EMAIL_JSON, getMessage(INVALID_VALUE_EMAIL)),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_MAX_AGE_JSON, getMessage(INVALID_VALUE_MAX_AGE)),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_MIN_AGE_JSON, getMessage(INVALID_VALUE_MIN_AGE)),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_NAME_JSON, getMessage(INVALID_VALUE_NAME)),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_PHONE_NUMBER_JSON, getMessage(INVALID_VALUE_PHONE_NUMBER)),
            Arguments.of(NEW_EMPLOYEE_WITH_INVALID_USERNAME_JSON, getMessage(INVALID_VALUE_USERNAME))
        );
    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @ParameterizedTest
    @MethodSource("provideArgsWhenDbConstraintIsViolatedOnEmployeeCreation")
    void shouldFailCreateEmployeeAndReturnStatus400WithErrMsgWhenDbConstraintIsViolated(String requestBodyFile, String errorMsg)
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees")
                                                            .content(getMappingFromInternalApi(requestBodyFile))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArgsWhenDbConstraintIsViolatedOnEmployeeCreation() {
        return Stream.of(
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_EMAIL_JSON, getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)),
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_PHONE_NUMBER_JSON, getMessage(DB_CONSTRAINT_VIOLATED_PHONE_NUMBER)),
            Arguments.of(NEW_EMPLOYEE_WITH_DUPLICATED_USERNAME_JSON, getMessage(DB_CONSTRAINT_VIOLATED_USERNAME))
        );
    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldUpdateEmployeeEmailAndReturnStatus204WhenEmailIsValidAndNotDuplicated()
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        patch(String.format("/v1/employees/%d", 1001))
                                                            .content(getMappingFromInternalApi(NEW_EMAIL_JSON))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isNoContent());
    }

    @Sql(scripts = "classpath:db/scripts/all_employees_data.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldFailUpdateEmployeeEmailAndReturnStatus400WithErrMsgWhenEmailIsValidAndDuplicated()
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        patch(String.format("/v1/employees/%d", 1001))
                                                            .content(getMappingFromInternalApi(NEW_EMAIL_WITH_DUPLICATED_VALUE_JSON))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)));
    }

    @Test
    void shouldFailUpdateEmployeeEmailAndReturnStatus404WithErrMsgWhenEmailIsValidAndEmployeeNotFound()
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        patch(String.format("/v1/employees/%d", 404))
                                                            .content(getMappingFromInternalApi(NEW_EMAIL_JSON))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isNotFound(), jsonPath("$.error").value(getMessage(ENTITY_NOT_FOUND)));
    }

    @Test
    void shouldFailUpdateEmployeeEmailAndReturnStatus400WithErrMsgWhenEmailIsInvalid()
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        patch(String.format("/v1/employees/%d", 1001))
                                                            .content(getMappingFromInternalApi(NEW_EMAIL_WITH_INVALID_VALUE_JSON))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(getMessage(INVALID_VALUE_EMAIL)));
    }

    @Sql(scripts = "classpath:db/scripts/new_employee.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_employees_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldDeleteEmployeeAndReturnStatus204WhenDataFound()
            throws Exception {
        
        var resultActions = getMockMvc().perform(
                                                        delete(String.format("/v1/employees/%d", 1001))
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isNoContent());
    }

    @Test
    void shouldFailDeleteEmployeeAndReturnStatus404WithErrMsgWhenDataNotFound()
            throws Exception {

        var resultActions = getMockMvc().perform(
                                                        delete(String.format("/v1/employees/%d", 404))
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isNotFound(), jsonPath("$.error").value(getMessage(ENTITY_NOT_FOUND)));
    }

    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldRegisterForScheduledMailAlertAndReturnStatus202WhenPayloadIsValidAndEmailNotDuplicated()
            throws Exception {
        var requestBody = getMappingFromInternalApi(NEW_RATE_ALERT_JSON);

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees/alerts")
                                                            .content(requestBody)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isAccepted());
    }

    @Sql(scripts = "classpath:db/scripts/new_rate_alert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void shouldFailRegisterForScheduledMailAlertAndReturnStatus400WithErrMsgWhenPayloadIsValidAndEmailDuplicated()
            throws Exception {
        var requestBody = getMappingFromInternalApi(NEW_RATE_ALERT_JSON);

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees/alerts")
                                                            .content(requestBody)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)));
    }

    @ParameterizedTest
    @MethodSource("provideArgsWhenPayloadIsInvalidOnMailAlertScheduling")
    void shouldFailRegisterForScheduledMailAlertAndReturnStatus400WithErrMsgWhenPayloadIsInvalid(String requestBodyFile, String errorMsg)
            throws Exception {
        var requestBody = getMappingFromInternalApi(requestBodyFile);

        var resultActions = getMockMvc().perform(
                                                        post("/v1/employees/alerts")
                                                            .content(requestBody)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArgsWhenPayloadIsInvalidOnMailAlertScheduling() {
        return Stream.of(
            Arguments.of(NEW_RATE_ALERT_WITH_INVALID_EMAIL_JSON, getMessage(INVALID_VALUE_EMAIL)),
            Arguments.of(NEW_RATE_ALERT_WITH_INVALID_BASE_JSON, getMessage(INVALID_VALUE_CURRENCY_CODE))
        );
    }

}
