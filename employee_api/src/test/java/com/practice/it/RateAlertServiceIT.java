package com.practice.it;

import static com.practice.utils.Mappings.DUPLICATED_EMAIL_JSON;
import static com.practice.utils.Mappings.INVALID_CURRENCY_CODE_JSON;
import static com.practice.utils.Mappings.INVALID_EMAIL_JSON;
import static com.practice.utils.Mappings.NEW_RATE_ALERT_JSON;
import static com.practice.utils.Mappings.NEW_RATE_ALERT_WITH_INVALID_BASE_JSON;
import static com.practice.utils.Mappings.NEW_RATE_ALERT_WITH_INVALID_EMAIL_JSON;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.mail.Message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.practice.application.ratealert.RateAlertServiceImpl;

class RateAlertServiceIT extends BaseControllerIT {

    @SpyBean
    private RateAlertServiceImpl rateAlertService;

    @Sql(scripts = "classpath:db/scripts/new_rate_alert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testSendScheduledMailAlert()
            throws Exception {
        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        await()
            .atMost(20, TimeUnit.SECONDS)
            .ignoreNoExceptions()
            .untilAsserted(() -> verify(rateAlertService).sendScheduledMailAlert());

        greenMail.waitForIncomingEmail(1);
        Message[] messages = greenMail.getReceivedMessages();
        assertTrue(messages != null && messages.length == 1);
        assertTrue(messages[0].getSubject().equals("Scheduled rate alerts"));
        assertTrue(messages[0].getFrom()[0].toString().equals("no-reply@via.com"));
        assertTrue(messages[0].getAllRecipients()[0].toString().equals("dorian@test.com"));
        greenMail.stop();
    }

    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testRegisterForScheduledMailAlert_WhenPayloadIsValidAndEmailNotDuplicated_ThenSaveAndReturn202()
            throws Exception {
        String requestBody = getMappingFromInternalApi(NEW_RATE_ALERT_JSON);

        ResultActions resultActions = getMockMvc().perform(
                                                        post("/v1/alerts/rates")
                                                        .content(requestBody)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isAccepted());
    }

    @Sql(scripts = "classpath:db/scripts/new_rate_alert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testRegisterForScheduledMailAlert_WhenPayloadIsValidAndEmailDuplicated_ThenReturn400WithErrorMsg()
            throws Exception {
        String requestBody = getMappingFromInternalApi(NEW_RATE_ALERT_JSON);

        ResultActions resultActions = getMockMvc().perform(
                                                    post("/v1/alerts/rates")
                                                    .content(requestBody)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(DUPLICATED_EMAIL_JSON), true));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestRegisterForScheduledMailAlertWhenPayloadIsInvalid")
    void testRegisterForScheduledMailAlert_WhenPayloadIsInvalid_ThenReturn400WithErrorMsg(String requestBodyFile, String errorMsgFile)
            throws Exception {
        String requestBody = getMappingFromInternalApi(requestBodyFile);

        ResultActions resultActions = getMockMvc().perform(
                                                    post("/v1/alerts/rates")
                                                    .content(requestBody)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(getMappingFromInternalApi(errorMsgFile), true));
    }

    private static Stream<Arguments> provideArgumentsForTestRegisterForScheduledMailAlertWhenPayloadIsInvalid() {
        return Stream.of(
            Arguments.of(NEW_RATE_ALERT_WITH_INVALID_EMAIL_JSON, INVALID_EMAIL_JSON),
            Arguments.of(NEW_RATE_ALERT_WITH_INVALID_BASE_JSON, INVALID_CURRENCY_CODE_JSON)
        );
    }

}
