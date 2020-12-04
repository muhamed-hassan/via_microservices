package com.practice.it;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.practice.application.ratealert.RateAlertJob;

class RateAlertServiceIT extends BaseControllerIT {

    @SpyBean
    private RateAlertJob rateAlertJob;

    @Sql(scripts = "classpath:db/scripts/new_rate_alert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/scripts/reset_rate_alert_table.sql", executionPhase = AFTER_TEST_METHOD)
    @Test
    void testSendScheduledMailAlert()
            throws Exception {
        var greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        await()
            .atMost(65, TimeUnit.SECONDS)
            .ignoreNoExceptions()
            .untilAsserted(() -> verify(rateAlertJob).sendScheduledMailAlert());

        greenMail.waitForIncomingEmail(1);
        var messages = greenMail.getReceivedMessages();
        assertTrue(messages != null && messages.length == 1);
        assertTrue(messages[0].getSubject().equals("Scheduled rate alerts"));
        assertTrue(messages[0].getFrom()[0].toString().equals("no-reply@via.com"));
        assertTrue(messages[0].getAllRecipients()[0].toString().equals("dorian@test.com"));
        greenMail.stop();
    }

}
