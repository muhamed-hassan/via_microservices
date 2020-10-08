package com.practice.application.ratealert;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import com.practice.application.shared.ServiceErrorHandler;
import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.infrastructure.integration.CurrencyConversionClient;

@ExtendWith(MockitoExtension.class)
class RateAlertServiceTest {

    private static final String DEFAULT_SENDER = "no-reply@via.com";

    private static final String DEFAULT_SUBJECT = "Hello dear customer";

    private static final int CHUNK_SIZE = 50;

    private RateAlertService rateAlertService;

    private RateAlertRepository rateAlertRepository;

    private CurrencyConversionClient currencyConversionClient;

    private MailSender mailSender;

    private ITemplateEngine templateEngine;

    private ServiceErrorHandler serviceErrorHandler;

    @BeforeEach
    void injectRefs() {
        rateAlertRepository = mock(RateAlertRepository.class);
        currencyConversionClient = mock(CurrencyConversionClient.class);
        mailSender = mock(MailSender.class);
        templateEngine = mock(ITemplateEngine.class);
        serviceErrorHandler = mock(ServiceErrorHandler.class);
        rateAlertService = new RateAlertServiceImpl(rateAlertRepository, currencyConversionClient, mailSender, templateEngine,
                                                        serviceErrorHandler, DEFAULT_SENDER, DEFAULT_SUBJECT, CHUNK_SIZE);
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsNew_ThenCreateIt() {
        RateAlert entity = mock(RateAlert.class);
        when(rateAlertRepository.save(any(RateAlert.class)))
            .thenReturn(entity);

        rateAlertService.registerForScheduledMailAlert(new RateAlert());

        verify(rateAlertRepository).save(any(RateAlert.class));
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsDuplicated_ThenThrowIllegalArgumentException() {
        doThrow(DataIntegrityViolationException.class)
            .when(rateAlertRepository).save(any(RateAlert.class));
        when(serviceErrorHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException("DB constraint is violated for this field: email"));

        assertThrows(IllegalArgumentException.class,
            () -> rateAlertService.registerForScheduledMailAlert(new RateAlert()));
    }

    @Test
    public void testSendScheduledMailAlert_WhenTriggeringTheJob_ThenSendEmailsWithResult()
            throws InterruptedException {
        String base = "ISK";
        List<String> bases = List.of(base);
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(bases);
        when(currencyConversionClient.getLatestRatesByBase(anyString()))
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

        rateAlertService.sendScheduledMailAlert();

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

        rateAlertService.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
    }

}
