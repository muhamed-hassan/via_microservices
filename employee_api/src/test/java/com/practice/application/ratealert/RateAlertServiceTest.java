package com.practice.application.ratealert;

import static com.practice.utils.ErrorKeys.DB_CONSTRAINT_VIOLATED_EMAIL;
import static com.practice.utils.ErrorMsgsCache.getMessage;
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

import com.practice.application.shared.ServiceExceptionHandler;
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

    private ServiceExceptionHandler serviceExceptionHandler;

    @BeforeEach
    void injectRefs() {
        rateAlertRepository = mock(RateAlertRepository.class);
        currencyConversionClient = mock(CurrencyConversionClient.class);
        mailSender = mock(MailSender.class);
        templateEngine = mock(ITemplateEngine.class);
        serviceExceptionHandler = mock(ServiceExceptionHandler.class);
        rateAlertService = new RateAlertServiceImpl(rateAlertRepository, currencyConversionClient, mailSender, templateEngine,
                                                        serviceExceptionHandler, DEFAULT_SENDER, DEFAULT_SUBJECT, CHUNK_SIZE);
    }

    @Test
    void testRegisterForScheduledMailAlertWhenEmailIsNewThenCreateIt() {
        var entity = mock(RateAlert.class);
        when(rateAlertRepository.save(any(RateAlert.class)))
            .thenReturn(entity);

        rateAlertService.registerForScheduledMailAlert(new RateAlert());

        verify(rateAlertRepository).save(any(RateAlert.class));
    }

    @Test
    void testRegisterForScheduledMailAlertWhenEmailIsDuplicatedThenThrowIllegalArgumentException() {
        doThrow(DataIntegrityViolationException.class)
            .when(rateAlertRepository).save(any(RateAlert.class));
        when(serviceExceptionHandler.wrapDataIntegrityViolationException(any(DataIntegrityViolationException.class), any(Class.class)))
            .thenReturn(new IllegalArgumentException(getMessage(DB_CONSTRAINT_VIOLATED_EMAIL)));

        assertThrows(IllegalArgumentException.class,
            () -> rateAlertService.registerForScheduledMailAlert(new RateAlert()));
    }

    @Test
    void testSendScheduledMailAlertWhenTriggeringTheJobThenSendEmailsWithResult()
            throws InterruptedException {
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

        rateAlertService.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
        verify(rateAlertRepository).count();
        verify(rateAlertRepository).findAll(any(Pageable.class));
        verify(templateEngine).process(anyString(), any(IContext.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private Map<String, Double> latestRatesOfIsk() {
        var rates = new HashMap<String, Double>();
        rates.put("CAD", 0.0094669118);
        rates.put("HKD", 0.0559742647);
        rates.put("ISK", 1.0);
        rates.put("PHP", 0.3512561275);
        rates.put("DKK", 0.0456078431);
        rates.put("HUF", 2.2071078431);
        rates.put("CZK", 0.167622549);
        rates.put("GBP", 0.0055509191);
        rates.put("RON", 0.0298713235);
        rates.put("SEK", 0.0633762255);
        rates.put("IDR", 106.6376838235);
        rates.put("INR", 0.529564951);
        rates.put("BRL", 0.0400667892);
        rates.put("RUB", 0.5563976716);
        rates.put("HRK", 0.0464368873);
        rates.put("JPY", 0.7621323529);
        rates.put("THB", 0.2247303922);
        rates.put("CHF", 0.0065772059);
        rates.put("EUR", 0.006127451);
        rates.put("MYR", 0.0299154412);
        rates.put("BGN", 0.0119840686);
        rates.put("TRY", 0.0570802696);
        rates.put("CNY", 0.0486446078);
        rates.put("NOK", 0.0661292892);
        rates.put("NZD", 0.0108547794);
        rates.put("ZAR", 0.1191458333);
        rates.put("USD", 0.0072224265);
        rates.put("MXN", 0.1532984069);
        rates.put("SGD", 0.0098082108);
        rates.put("AUD", 0.0100508578);
        rates.put("ILS", 0.0244454657);
        rates.put("KRW", 8.2781862745);
        rates.put("PLN", 0.0274822304);
        return rates;
    }

    @Test
    void testSendScheduledMailAlertWhenNoBasesExistInDbThenDoNothing()
            throws InterruptedException {
        when(rateAlertRepository.findAllDistinctBases())
            .thenReturn(List.of());

        rateAlertService.sendScheduledMailAlert();

        verify(rateAlertRepository).findAllDistinctBases();
    }

}
