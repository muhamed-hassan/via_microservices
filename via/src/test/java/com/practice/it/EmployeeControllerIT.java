package com.practice.it;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.practice.helpers.IDs.VALID_AGE;
import static com.practice.helpers.IDs.INVALID_AGE;
import static com.practice.helpers.IDs.EXISTING_ID;
import static com.practice.helpers.IDs.NON_EXISTING_ID;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import com.practice.web.dtos.EmailDto;
import com.practice.web.dtos.EmployeeDto;
import com.practice.web.dtos.RateAlertDto;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EmployeeControllerIT extends BaseControllerIT {

    private static final String EXPECTED_EMPLOYEES_FROM_CURRENT_API = EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR + "employees.json";

    @Test
    public void testGetEmployees() throws Exception {
        String expectedResponse = readJsonFrom(EXPECTED_EMPLOYEES_FROM_CURRENT_API);

        ResultActions resultActions = getMockMvc().perform(get("/api/v1/employees"));

        resultActions.andExpect(status().isOk())
            			.andExpect(content().json(expectedResponse, false));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetEmployeeByFieldCriteria")
    public void testGetEmployeeByFieldCriteria(final String pathVariable, final ResultMatcher expectedHttpStatusMatcher, final ResultMatcher expectedContentMatcher) throws Exception {
        ResultActions resultActions = getMockMvc().perform(get(MessageFormat.format("/api/v1/employees/criteria/{0}", pathVariable)));

        resultActions.andExpect(expectedHttpStatusMatcher)
            			.andExpect(expectedContentMatcher);
    }

    private static Stream<Arguments> provideArgumentsForTestGetEmployeeByFieldCriteria() {
        StringBuilder expectedValidResponse = new StringBuilder()
            .append("{")
            .append("\"id\": 1,")
            .append("\"name\": \"Charlotte\",")
            .append("\"username\": \"Charlotte_Jack\",")
            .append("\"email\": \"Charlotte_Jack@test.com\",")
            .append("\"phone_number\": \"061123456\",")
            .append("\"age\": 25")
            .append("}");
        return Stream.of(
            Arguments.of("id:1", status().isOk(), content().json(expectedValidResponse.toString(), true)),
            Arguments.of("email:Charlotte_Jack@test.com", status().isOk(), content().json(expectedValidResponse.toString(), true)),
            Arguments.of("username:Charlotte_Jack", status().isOk(), content().json(expectedValidResponse.toString(), true)),
            Arguments.of("id1", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("id=1", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("emailCharlotte_Jack@test.com", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("email=Charlotte_Jack@test.com", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("usernameCharlotte_Jack", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("username=Charlotte_Jack", status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of("phone_number:061123456", status().isNotImplemented(), content().string(containsString("error"))),
            Arguments.of("age:35", status().isNotImplemented(), content().string(containsString("error")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestCreateEmployee")
    public void testCreateEmployee(final EmployeeDto employeeDto, final ResultMatcher expectedHttpStatusMatcher, final ResultMatcher expectedContentMatcher) throws Exception {
        ResultActions resultActions = getMockMvc().perform(post("/api/v1/employees")
													.content(getObjectMapper().writeValueAsString(employeeDto))
													.contentType(MediaType.APPLICATION_JSON)
													.characterEncoding(StandardCharsets.UTF_8.name()));

        resultActions.andExpect(expectedHttpStatusMatcher)
            			.andExpect(expectedContentMatcher);
    }

    private static Stream<Arguments> provideArgumentsForTestCreateEmployee() {
        EmployeeDto employeeDtoWithValidData = EmployeeDto.getBuilder().name("Ronan").username("Ronan_Farhan").email("Ronan_Farhan@test.com").phoneNumber("066123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithAlreadyExistedUsername = EmployeeDto.getBuilder().name("Charlotte").username("Charlotte_Jack").email("Charlotte_Jacky@test.com").phoneNumber("0670123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithAlreadyExistedEmail = EmployeeDto.getBuilder().name("Charlotte").username("Charlotte_Ronan").email("Charlotte_Jack@test.com").phoneNumber("0660123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithAlreadyExistedPhoneNumber = EmployeeDto.getBuilder().name("Charlotte").username("Charlotte_Farhan").email("Charlotte_Farhan@test.com").phoneNumber("061123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithInvalidName = EmployeeDto.getBuilder().name("$$$$$$$$$$$$$").username("Ronan_Farhan").email("Ronan_Farhan@test.com").phoneNumber("066123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithInvalidUsername = EmployeeDto.getBuilder().name("Ronan").username("$$$$$$$$$$$$$").email("Ronan_Farhan@test.com").phoneNumber("066123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithInvalidEmail = EmployeeDto.getBuilder().name("Ronan").username("Ronan_Farhan").email("Ronan_Farhantest.com").phoneNumber("066123456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithInvalidPhoneNumber = EmployeeDto.getBuilder().name("Ronan").username("Ronan_Farhan").email("Ronan_Farhan@test.com").phoneNumber("0661dddd23456").age(VALID_AGE).build();
        EmployeeDto employeeDtoWithInvalidAge = EmployeeDto.getBuilder().name("Ronan").username("Ronan_Farhan").email("Ronan_Farhan@test.com").phoneNumber("066123456").age(
            INVALID_AGE).build();

        return Stream.of(
            Arguments.of(employeeDtoWithValidData, status().isCreated(), header().exists("Location")),
            Arguments.of(employeeDtoWithAlreadyExistedUsername, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithAlreadyExistedEmail, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithAlreadyExistedPhoneNumber, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithInvalidName, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithInvalidUsername, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithInvalidEmail, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithInvalidPhoneNumber, status().isBadRequest(), content().string(containsString("error"))),
            Arguments.of(employeeDtoWithInvalidAge, status().isBadRequest(), content().string(containsString("error")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForUpdateEmployeeEmailById")
    public void testUpdateEmployeeEmailById(final EmailDto emailDto, final long employeeId, final ResultMatcher expectedOutcomeMatcher) throws Exception {
        ResultActions resultActions = getMockMvc().perform(patch(MessageFormat.format("/api/v1/employees/{0}", employeeId))
													.content(getObjectMapper().writeValueAsString(emailDto))
													.contentType(MediaType.APPLICATION_JSON)
													.characterEncoding(StandardCharsets.UTF_8.name()));

        resultActions.andExpect(expectedOutcomeMatcher);
    }

    private static Stream<Arguments> provideArgumentsForUpdateEmployeeEmailById() {
        EmailDto newEmail = new EmailDto("NEW_Charlotte_Jack@test.com");
        EmailDto emailAlreadyExist = new EmailDto("Charlotte_Jack@test.com");
        EmailDto invalidEmail = new EmailDto("NEW_Charlotte_Jacktest.com");

        return Stream.of(
            Arguments.of(newEmail, EXISTING_ID, status().isNoContent()),
            Arguments.of(emailAlreadyExist, EXISTING_ID, status().isBadRequest()),
            Arguments.of(newEmail, NON_EXISTING_ID, status().isBadRequest()),
            Arguments.of(invalidEmail, EXISTING_ID, status().isBadRequest())
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestDeleteEmployeeById")
    public void testDeleteEmployeeById(final long employeeId, final ResultMatcher expectedOutcomeMatcher) throws Exception {
        ResultActions resultActions = getMockMvc().perform(delete(MessageFormat.format("/api/v1/employees/{0}", employeeId)));

        resultActions.andExpect(expectedOutcomeMatcher);
    }

    private static Stream<Arguments> provideArgumentsForTestDeleteEmployeeById() {
        return Stream.of(
            Arguments.of(EXISTING_ID, status().isNoContent()),
            Arguments.of(NON_EXISTING_ID, status().isNotFound())
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestRegisterForScheduledMailAlert")
    public void testRegisterForScheduledMailAlert(final RateAlertDto rateAlertDto, final ResultMatcher expectedOutcomeMatcher) throws Exception {
        ResultActions resultActions = getMockMvc().perform(post("/api/v1/employees/alerts/rates")
													.content(getObjectMapper().writeValueAsString(rateAlertDto))
													.contentType(MediaType.APPLICATION_JSON)
													.characterEncoding(StandardCharsets.UTF_8.name()));

        resultActions.andExpect(expectedOutcomeMatcher);
    }

    private static Stream<Arguments> provideArgumentsForTestRegisterForScheduledMailAlert() {
        RateAlertDto rateAlertDtoWithNewEmail = new RateAlertDto();
        rateAlertDtoWithNewEmail.setEmail("Harrison_Carlos@test.com");
        rateAlertDtoWithNewEmail.setBase("HUF");

        RateAlertDto rateAlertDtoWithEmailAlreadyExist = new RateAlertDto();
        rateAlertDtoWithEmailAlreadyExist.setEmail("Charlotte_Jack@test.com");
        rateAlertDtoWithEmailAlreadyExist.setBase("HUF");

        RateAlertDto rateAlertDtoWithInvalidEmail = new RateAlertDto();
        rateAlertDtoWithInvalidEmail.setEmail("Harrison_Carlostest.com");
        rateAlertDtoWithInvalidEmail.setBase("HUF");

        return Stream.of(
            Arguments.of(rateAlertDtoWithNewEmail, status().isAccepted()),
            Arguments.of(rateAlertDtoWithEmailAlreadyExist, status().isBadRequest()),
            Arguments.of(rateAlertDtoWithInvalidEmail, status().isBadRequest())
        );
    }

}
