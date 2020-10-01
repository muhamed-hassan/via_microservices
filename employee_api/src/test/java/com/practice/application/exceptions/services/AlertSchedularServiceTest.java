package com.practice.application.exceptions.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import com.practice.infrastructure.integration.CurrencyConversionProvider;
import com.practice.domain.persistence.entities.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.application.ratealert.RateAlertServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AlertSchedularServiceTest {

    @InjectMocks
    private RateAlertServiceImpl alertSchedularService;

    @Mock
    private RateAlertRepository rateAlertRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private CurrencyConversionProvider currencyConversionProvider;

    @Test
    public void testSendScheduledMailAlert_WhenTriggeringTheJob_ThenSendEmailsWithResult()
            throws InterruptedException {
        ReflectionTestUtils.setField(alertSchedularService, "defaultSender", "SENDER");
        ReflectionTestUtils.setField(alertSchedularService, "defaultSubject", "SUBJECT");
        ReflectionTestUtils.setField(alertSchedularService, "chunkSize", 50);
        String base = "ISK";
        List<String> bases = List.of(base);
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(bases);
        when(currencyConversionProvider.getLatestRatesByBase(anyString()))
            .thenReturn(getLatestRatesOfIsk());
        when(rateAlertRepository.count())
            .thenReturn(1L);
        RateAlert rateAlert = new RateAlert();
        rateAlert.setId(1);
        rateAlert.setBase(base);
        rateAlert.setEmail("email@example.com");
        Page<RateAlert> resultPage = mock(Page.class);
        List<RateAlert> result = List.of(rateAlert);
        when(rateAlertRepository.findAll(any(Pageable.class)))
            .thenReturn(resultPage);
        when(resultPage.toList())
            .thenReturn(result);
        when(templateEngine.process(anyString(), any(IContext.class)))
            .thenReturn("xyz");
        doNothing()
            .when(mailSender).send(any(SimpleMailMessage.class));

        alertSchedularService.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
        verify(rateAlertRepository).count();
        verify(rateAlertRepository).findAll(any(Pageable.class));
        verify(templateEngine).process(anyString(), any(IContext.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private Map<String, Double> getLatestRatesOfIsk() {
        Map<String, Double> response = new HashMap<>();
        response.put("CAD", 0.009858125);
        response.put("HKD", 0.056986875);
        response.put("ISK", 1.0);
        response.put("PHP", 0.3610875);
        response.put("DKK", 0.04653875);
        response.put("HUF", 2.1545625);
        response.put("CZK", 0.16388125);
        response.put("GBP", 0.0056459375);
        response.put("RON", 0.03022375);
        response.put("SEK", 0.064390625);
        response.put("IDR", 107.980625);
        response.put("INR", 0.552378125);
        response.put("BRL", 0.039214375);
        response.put("RUB", 0.542046875);
        response.put("HRK", 0.046676875);
        response.put("JPY", 0.779875);
        response.put("THB", 0.2288);
        response.put("CHF", 0.006725625);
        response.put("EUR", 0.00625);
        response.put("MYR", 0.03103375);
        response.put("BGN", 0.01222375);
        response.put("TRY", 0.05124375);
        response.put("CNY", 0.051348125);
        response.put("NOK", 0.06723);
        response.put("NZD", 0.011129375);
        response.put("ZAR", 0.128049375);
        response.put("USD", 0.007353125);
        response.put("MXN", 0.167125625);
        response.put("SGD", 0.010121875);
        response.put("AUD", 0.010309375);
        response.put("ILS", 0.0251825);
        response.put("KRW", 8.7905625);
        response.put("PLN", 0.027534375);
        return response;
    }

    @Test
    public void testSendScheduledMailAlert_WhenNoBasesExistInDb_ThenDoNothing()
            throws InterruptedException {
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(List.of());

        alertSchedularService.sendScheduledMailAlert();

        verify(rateAlertRepository, times(1)).findAllDistinctBases();
    }

}
