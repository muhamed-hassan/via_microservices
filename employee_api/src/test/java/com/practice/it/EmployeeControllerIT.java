package com.practice.it;

import static com.practice.utils.HttpClient.doRequest;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.practice.it.helpers.models.HttpRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "via.scheduling.enable=false")
@ActiveProfiles("test")
public class EmployeeControllerIT /*extends BaseControllerIT*/ {

    private static final String UPDATE_SCRIPTS_DIR = "db/scripts/";

    private static PostgreSQLContainer postgreSQLContainer;

    @BeforeAll
    public static void initTestDB() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer("postgres:12")
                .withDatabaseName("integration-tests-db")
                .withUsername("username")
                .withPassword("password");
            postgreSQLContainer.start();
            System.setProperty("DB_URL", postgreSQLContainer.getJdbcUrl());
            System.setProperty("DB_USER", postgreSQLContainer.getUsername());
            System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());
        }
    }

    public void updateTestDB(String scriptName)
        throws SQLException, URISyntaxException, IOException {
        String script = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(UPDATE_SCRIPTS_DIR + scriptName).toURI()))
            .stream()
            .collect(Collectors.joining());
        Connection connection = postgreSQLContainer.createConnection("");
        Statement statement = connection.createStatement();
        statement.execute(script);
    }

    @Test
    public void testGetEmployees_WhenDataFound_ThenReturn200WithData() throws Exception {
        updateTestDB("all_employees_data.sql");
        String expectedResponse = getMappingFromInternalApi("all_employees.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees", headers, HttpMethod.GET), String.class);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        JSONAssert.assertEquals(expectedResponse, actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
        updateTestDB("reset_employees_table.sql");
    }

    @Test
    public void testGetEmployees_WhenDataNotFound_ThenReturn404WithErrorMsg() throws Exception {
        String expectedResponse = getMappingFromInternalApi("no-data-found.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees", headers, HttpMethod.GET), String.class);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        JSONAssert.assertEquals(expectedResponse, actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataFound")
    public void testGetEmployeeByFieldCriteria_WhenDataFound_ThenReturn200WithData
        (String pathVariable) throws Exception {
        updateTestDB("employee_with_criteria.sql");
        String expectedResponse = getMappingFromInternalApi("employee-with-criteria.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/criteria/{0}", pathVariable),
            headers, HttpMethod.GET), String.class);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        JSONAssert.assertEquals(expectedResponse, actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
        updateTestDB("reset_employees_table.sql");
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataFound() {
        return Stream.of(
            Arguments.of("id:1"),
            Arguments.of("email:wanya@test.com"),
            Arguments.of("username:wanya_costrau")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataNotFound")
    public void testGetEmployeeByFieldCriteria_WhenDataNotFound_ThenReturn404WithErrorMsg
        (String pathVariable) throws Exception {
        String expectedResponse = getMappingFromInternalApi("entity-not-found.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/criteria/{0}", pathVariable),
            headers, HttpMethod.GET), String.class);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        JSONAssert.assertEquals(expectedResponse, actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteriaWhenDataNotFound() {
        return Stream.of(
            Arguments.of("id:2"),
            Arguments.of("email:anna@test.com"),
            Arguments.of("username:anna_nyporka")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteriaWhenSendingInvalidCriteria")
    public void testGetEmployeeByFieldCriteria_WhenSendingInvalidCriteria_ThenReturn400WithErrorMsg
        (String pathVariable) throws Exception {
        String expectedResponse = getMappingFromInternalApi("invalid-field-criteria.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/criteria/{0}", pathVariable),
            headers, HttpMethod.GET), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(expectedResponse, actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
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
    public void testCreateEmployee_WhenPayloadIsValid_ThenSaveItAndReturn201WithItsLocation
        () {
        String requestBody = getMappingFromInternalApi("new-employee.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<Void> response = doRequest(HttpRequest.from("/v1/employees", headers, HttpMethod.POST, requestBody), Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().getPath().matches("^\\/v1\\/employees\\/[1-9][0-9]*$"));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestCreateEmployeeWhenPayloadIsInvalid")
    public void testCreateEmployee_WhenPayloadIsInvalid_ThenReturn400WithErrorMsg
        (String requestBodyFile, String errorMsgFile) throws Exception {
        String requestBody = getMappingFromInternalApi(requestBodyFile);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees", headers, HttpMethod.POST, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi(errorMsgFile), actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestCreateEmployeeWhenPayloadIsInvalid() {

        return Stream.of(
            Arguments.of("new-employee-with-invalid-email.json", "invalid-email.json"),
            Arguments.of("new-employee-with-invalid-max-age.json", "invalid-max-age.json"),
            Arguments.of("new-employee-with-invalid-min-age.json", "invalid-min-age.json"),
            Arguments.of("new-employee-with-invalid-name.json", "invalid-name.json"),
            Arguments.of("new-employee-with-invalid-phone-number.json", "invalid-phone-number.json"),
            Arguments.of("new-employee-with-invalid-username.json", "invalid-username.json")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestCreateEmployeeWhenDbConstraintIsViolated")
    public void testCreateEmployee_WhenDbConstraintIsViolated_ThenReturn400WithErrorMsg
        (String requestBodyFile, String errorMsgFile) throws Exception {
        updateTestDB("new_employee.sql");
        String requestBody = getMappingFromInternalApi(requestBodyFile);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees", headers, HttpMethod.POST, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi(errorMsgFile), actualResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
        updateTestDB("reset_employees_table.sql");
    }

    private static Stream<Arguments> provideArgumentsForTestCreateEmployeeWhenDbConstraintIsViolated() {

        return Stream.of(
            Arguments.of("new-employee-with-duplicated-email.json", "duplicated-email.json"),
            Arguments.of("new-employee-with-duplicated-phone-number.json", "duplicated-phone-number.json"),
            Arguments.of("new-employee-with-duplicated-username.json", "duplicated-username.json")
        );
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmailIsValidAndNotDuplicated_ThenUpdateAndReturn204() throws SQLException, IOException, URISyntaxException {
        updateTestDB("new_employee.sql");
        String requestBody = getMappingFromInternalApi("new-email.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<Void> response = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 1), headers, HttpMethod.PATCH, requestBody),
            Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        updateTestDB("reset_employees_table.sql");
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmailIsValidAndDuplicated_ThenReturn400WithErrorMsg()
        throws SQLException, IOException, URISyntaxException, JSONException {
        updateTestDB("all_employees_data.sql");
        String requestBody = getMappingFromInternalApi("new-email-with-duplicated-value.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(
            HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 1), headers, HttpMethod.PATCH, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi("duplicated-email.json"), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
        updateTestDB("reset_employees_table.sql");
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmailIsValidAndEmployeeNotFound_ThenReturn404WithErrorMsg()
        throws SQLException, IOException, URISyntaxException, JSONException {
        String requestBody = getMappingFromInternalApi("new-email.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(
            HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 404), headers, HttpMethod.PATCH, requestBody), String.class);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi("entity-not-found.json"), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testUpdateEmployeeEmailById_WhenEmailIsInvalid_ThenReturn400WithErrorMsg() throws JSONException {
        String requestBody = getMappingFromInternalApi("new-email-with-invalid-value.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(
            HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 1), headers, HttpMethod.PATCH, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi("invalid-email.json"), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testDeleteEmployeeById_WhenDataFound_ThenDeleteAndReturn204() throws SQLException, IOException, URISyntaxException {
        updateTestDB("all_employees_data.sql");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 1),
            headers, HttpMethod.DELETE), String.class);

        assertEquals(HttpStatus.NO_CONTENT, actualResponse.getStatusCode());
        updateTestDB("reset_employees_table.sql");
    }

    @Test
    public void testDeleteEmployeeById_WhenDataNotFound_ThenReturn404WithErrorMsg() throws JSONException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from(MessageFormat.format("/v1/employees/{0}", 404),
            headers, HttpMethod.DELETE), String.class);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi("entity-not-found.json"), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenPayloadIsValidAndEmailNotDuplicated_ThenSaveAndReturn202()
        throws SQLException, IOException, URISyntaxException {
        String requestBody = getMappingFromInternalApi("new-rate-alert.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees/alerts/rates",
            headers, HttpMethod.POST, requestBody), String.class);

        assertEquals(HttpStatus.ACCEPTED, actualResponse.getStatusCode());
        updateTestDB("reset_rate_alert_table.sql");
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenPayloadIsValidAndEmailDuplicated_ThenReturn400WithErrorMsg()
        throws SQLException, IOException, URISyntaxException, JSONException {
        updateTestDB("new_rate_alert.sql");
        String requestBody = getMappingFromInternalApi("new-rate-alert.json");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees/alerts/rates",
            headers, HttpMethod.POST, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi("duplicated-email.json"), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
        updateTestDB("reset_rate_alert_table.sql");
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestRegisterForScheduledMailAlertWhenPayloadIsInvalid")
    public void testRegisterForScheduledMailAlert_WhenPayloadIsInvalid_ThenReturn400WithErrorMsg(
        String requestBodyFile, String errorMsgFile
    )
        throws JSONException {
        String requestBody = getMappingFromInternalApi(requestBodyFile);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> actualResponse = doRequest(HttpRequest.from("/v1/employees/alerts/rates",
            headers, HttpMethod.POST, requestBody), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        JSONAssert.assertEquals(getMappingFromInternalApi(errorMsgFile), actualResponse.getBody(),
            JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestRegisterForScheduledMailAlertWhenPayloadIsInvalid() {

        return Stream.of(
            Arguments.of("new-rate-alert-with-invalid-email.json", "invalid-email.json"),
            Arguments.of("new-rate-alert-with-invalid-base.json", "invalid-currency-code.json")
        );
    }

}
