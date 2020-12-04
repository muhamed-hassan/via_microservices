package com.practice.application.ratealert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.infrastructure.integration.CurrencyConversionClient;
import com.practice.infrastructure.integration.models.Rate;

@ExtendWith(MockitoExtension.class)
class RateAlertJobTest {

    private static final String DEFAULT_SENDER = "no-reply@via.com";

    private static final String DEFAULT_SUBJECT = "Hello dear customer";

    private static final int CHUNK_SIZE = 50;

    private RateAlertJob rateAlertJob;

    private RateAlertRepository rateAlertRepository;

    private CurrencyConversionClient currencyConversionClient;

    private MailSender mailSender;

    private ITemplateEngine templateEngine;

    @BeforeEach
    void injectRefs() {
        rateAlertRepository = mock(RateAlertRepository.class);
        currencyConversionClient = mock(CurrencyConversionClient.class);
        mailSender = mock(MailSender.class);
        templateEngine = mock(ITemplateEngine.class);
        rateAlertJob = new RateAlertJob(rateAlertRepository, currencyConversionClient, mailSender, templateEngine,
                                        DEFAULT_SENDER, DEFAULT_SUBJECT, CHUNK_SIZE);
    }

    @Test
    void testSendScheduledMailAlertWhenTriggeringTheJobThenSendEmailsWithResult() {
        var base = "ISK";
        var bases = List.of(base);
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(bases);
        when(currencyConversionClient.getLatestRatesByBase(anyString()))
            .thenReturn(latestRatesOfIsk());
        when(rateAlertRepository.count())
            .thenReturn(1L);
        var rateAlert = new RateAlert();
        rateAlert.setId(1L);
        rateAlert.setBase(base);
        rateAlert.setEmail("email@example.com");
        var resultPage = mock(Page.class);
        var result = List.of(rateAlert);
        when(rateAlertRepository.findAll(any(Pageable.class)))
            .thenReturn(resultPage);
        when(resultPage.toList())
            .thenReturn(result);
        when(templateEngine.process(anyString(), any(IContext.class)))
            .thenReturn("xyz");
        doNothing()
            .when(mailSender).send(any(SimpleMailMessage.class));

        rateAlertJob.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
        verify(rateAlertRepository).count();
        verify(rateAlertRepository).findAll(any(Pageable.class));
        verify(templateEngine).process(anyString(), any(IContext.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private List<Rate> latestRatesOfIsk() {
        var rates = new ArrayList<Rate>();
        rates.add(new Rate("CAD", 0.0094669118));
        rates.add(new Rate("HKD", 0.0559742647));
        rates.add(new Rate("ISK", 1.0));
        rates.add(new Rate("PHP", 0.3512561275));
        rates.add(new Rate("DKK", 0.0456078431));
        rates.add(new Rate("HUF", 2.2071078431));
        rates.add(new Rate("CZK", 0.167622549));
        rates.add(new Rate("GBP", 0.0055509191));
        rates.add(new Rate("RON", 0.0298713235));
        rates.add(new Rate("SEK", 0.0633762255));
        rates.add(new Rate("IDR", 106.6376838235));
        rates.add(new Rate("INR", 0.529564951));
        rates.add(new Rate("BRL", 0.0400667892));
        rates.add(new Rate("RUB", 0.5563976716));
        rates.add(new Rate("HRK", 0.0464368873));
        rates.add(new Rate("JPY", 0.7621323529));
        rates.add(new Rate("THB", 0.2247303922));
        rates.add(new Rate("CHF", 0.0065772059));
        rates.add(new Rate("EUR", 0.006127451));
        rates.add(new Rate("MYR", 0.0299154412));
        rates.add(new Rate("BGN", 0.0119840686));
        rates.add(new Rate("TRY", 0.0570802696));
        rates.add(new Rate("CNY", 0.0486446078));
        rates.add(new Rate("NOK", 0.0661292892));
        rates.add(new Rate("NZD", 0.0108547794));
        rates.add(new Rate("ZAR", 0.1191458333));
        rates.add(new Rate("USD", 0.0072224265));
        rates.add(new Rate("MXN", 0.1532984069));
        rates.add(new Rate("SGD", 0.0098082108));
        rates.add(new Rate("AUD", 0.0100508578));
        rates.add(new Rate("ILS", 0.0244454657));
        rates.add(new Rate("KRW", 8.2781862745));
        rates.add(new Rate("PLN", 0.0274822304));
        return rates;
    }

    @Test
    void testSendScheduledMailAlertWhenNoBasesExistInDbThenDoNothing() {
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(List.of());

        rateAlertJob.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
    }

}
