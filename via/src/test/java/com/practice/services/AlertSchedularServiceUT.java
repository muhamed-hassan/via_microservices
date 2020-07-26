package com.practice.services;

import static com.practice.configs.constants.Messages.EMAIL_ALREADY_EXIST;
import static com.practice.persistence.entities.RateAlertEntity.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.IContext;

import com.practice.exceptions.DbConstraintViolationException;
import com.practice.exceptions.handlers.EmployeeServiceErrorHandler;
import com.practice.persistence.entities.RateAlertEntity;
import com.practice.persistence.repositories.RateAlertRepository;
import com.practice.services.helpers.TestTemplateEngine;
import com.practice.services.impl.AlertSchedularServiceImpl;
import com.practice.services.impl.CurrencyConversionServiceImpl;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.RateAlertDto;

@ExtendWith(MockitoExtension.class)
public class AlertSchedularServiceUT {

    @InjectMocks
    private AlertSchedularServiceImpl alertSchedularService;

    @Mock
    private RateAlertRepository rateAlertRepository;

    @Mock
    private EntityTransformer entityTransformer;

    @Mock
    private MailSender mailSender;

    @Mock
    private TestTemplateEngine templateEngine;

    @Mock
    private CurrencyConversionServiceImpl currencyConversionService;

    @Mock
    private EmployeeServiceErrorHandler employeeServiceErrorHandler;

    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsNew_ThenCreateIt() {
        RateAlertEntity entity = mock(RateAlertEntity.class);
        when(entityTransformer.toEntity(any(RateAlertDto.class), any(Class.class)))
            .thenReturn(entity);
        when(rateAlertRepository.save(any(RateAlertEntity.class)))
            .thenReturn(entity);

        alertSchedularService.registerForScheduledMailAlert(new RateAlertDto());

        verify(entityTransformer, times(1)).toEntity(any(RateAlertDto.class), any(Class.class));
        verify(rateAlertRepository, times(1)).save(any(RateAlertEntity.class));
    }

    @Test
    public void testRegisterForScheduledMailAlert_WhenEmailIsDuplicated_ThenThrowDbConstraintViolationException() {
        RateAlertEntity entity = mock(RateAlertEntity.class);
        when(entityTransformer.toEntity(any(RateAlertDto.class), any(Class.class)))
            .thenReturn(entity);
        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException("..." + RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL + "...");
        doThrow(dataIntegrityViolationException)
            .when(rateAlertRepository).save(entity);
        DbConstraintViolationException dbConstraintViolationException = new DbConstraintViolationException(EMAIL_ALREADY_EXIST, dataIntegrityViolationException);
        doThrow(dbConstraintViolationException)
            .when(employeeServiceErrorHandler).handleDataIntegrityViolationException(dataIntegrityViolationException);

        Executable executable = () -> alertSchedularService.registerForScheduledMailAlert(new RateAlertDto());
        DbConstraintViolationException thrown = assertThrows(DbConstraintViolationException.class, executable);
        assertTrue(thrown.getMessage().equals(EMAIL_ALREADY_EXIST));
    }

    @Test
    public void testSendScheduledMailAlert_WhenTriggeringTheJob_ThenSendEmailsWithResult() throws Exception {
        ReflectionTestUtils.setField(alertSchedularService, "defaultSender", "SENDER");
        ReflectionTestUtils.setField(alertSchedularService, "defaultSubject", "SUBJECT");
        ReflectionTestUtils.setField(alertSchedularService, "chunkSize", 50);
        String base = "HUF";
        Map<String, Double> response = Map.of("CAD", 0.0043762489,
            "HKD", 0.0241258921,
            "ISK", 0.4456180417);
        List<String> bases = List.of(base);
        when(rateAlertRepository.getBases())
            .thenReturn(bases);
        when(currencyConversionService.getLatestRatesByBase(anyString()))
            .thenReturn(response);
        when(rateAlertRepository.count())
            .thenReturn(1L);
        RateAlertEntity rateAlert = new RateAlertEntity();
        rateAlert.setId(1L);
        rateAlert.setBase(base);
        rateAlert.setEmail("email@example.com");
        Page<RateAlertEntity> resultPage = mock(Page.class);
        List<RateAlertEntity> result = List.of(rateAlert);
        when(rateAlertRepository.findAll(any(Pageable.class)))
            .thenReturn(resultPage);
        when(resultPage.toList())
            .thenReturn(result);
        String processedTemplate = "xyz";
        when(templateEngine.process(anyString(), any(IContext.class)))
            .thenReturn(processedTemplate);
        doNothing()
            .when(mailSender).send(any(SimpleMailMessage.class));

        alertSchedularService.sendScheduledMailAlert();

        verify(rateAlertRepository, times(1)).getBases();
        verify(rateAlertRepository, times(1)).count();
        verify(rateAlertRepository, times(1)).findAll(any(Pageable.class));
        verify(templateEngine, times(1)).process(anyString(), any(IContext.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendScheduledMailAlert_WhenNoBasesExistInDb_ThenDoNothing() throws Exception {
        when(rateAlertRepository.getBases())
            .thenReturn(List.of());

        alertSchedularService.sendScheduledMailAlert();

        verify(rateAlertRepository, times(1)).getBases();
    }

}
