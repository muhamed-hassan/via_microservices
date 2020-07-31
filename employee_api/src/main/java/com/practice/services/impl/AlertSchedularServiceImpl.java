package com.practice.services.impl;

import static com.practice.persistence.entities.RateAlert.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.practice.configs.constants.Messages;
import com.practice.exceptions.DbConstraintViolationException;

import com.practice.persistence.entities.Employee;
import com.practice.persistence.entities.RateAlert;
import com.practice.persistence.repositories.RateAlertRepository;
import com.practice.services.AlertSchedularService;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.RateAlertDto;

@Service
public class AlertSchedularServiceImpl implements AlertSchedularService {
    
//    @Autowired
//    private CurrencyConversionService currencyConversionService;

    @Autowired
    private RateAlertRepository rateAlertRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ITemplateEngine templateEngine;

    @Autowired
    private EntityTransformer entityTransformer;

//    @Autowired
//    private EmployeeServiceErrorHandler employeeServiceErrorHandler;

    @Value("${via.default-email.sender}")
    private String defaultSender;

    @Value("${via.default-email.subject}")
    private String defaultSubject;

    @Value("${chunk-size}")
    private int chunkSize;
    
    // assuming employee can register only for one rate
    @Transactional
    @Override
    public void registerForScheduledMailAlert(RateAlertDto rateAlertDto) {
        try {
            rateAlertRepository.save(entityTransformer.toEntity(rateAlertDto, RateAlert.class));
        } catch (DataIntegrityViolationException e) {
            String errorMsg = e.getMessage();
            String violatedField = Stream.of(Employee.class.getDeclaredFields())
                .map(Field::getName)
                .filter(fieldName -> errorMsg.matches(".*(_"+fieldName+"_).*"))
                .findFirst()
                .get();
            //            throw wrapDataIntegrityViolationException(e);
            // add custom msg according to the thrown exception msg
            throw new IllegalArgumentException("DB constraint is violated for this field: " + violatedField);
        }
    }

    @Scheduled(cron = "${via.scheduled-email.rate}")
    @Override
    public void sendScheduledMailAlert() throws InterruptedException {
        //1. aggregate bases from DB in List<base:String> -> available distinct bases in DB
        List<String> bases = rateAlertRepository.findAllDistinctBases();
        if (!bases.isEmpty()) {
            //2. for each base make a request to get it's data
            Map<String, Map<String, Double>> latestRates = getLatestRates(bases);
            int iterations = calculateIterationsLength();
            sendAlerts(latestRates, iterations);
        }
    }

    private Map<String, Map<String, Double>> getLatestRates(List<String> bases) throws InterruptedException {
        Map<String, Map<String, Double>> latestRates = new HashMap<>();
        int cursor;
        for (cursor = 0; cursor < bases.size(); cursor++) {
            String base = bases.get(cursor);
            //call feign client to get result from currency-conversion-api
            //endpoint: v1/rates?currencyCode=value
            Map<String, Double> latestRatesOfCurrentBase = null;//currencyConversionService.getLatestRatesByBase(base);
            if (latestRatesOfCurrentBase.isEmpty()) { // means that currency-conversion-api API is down
                cursor--; // move the cursor back 1 step, to re-try the failed request
                TimeUnit.HOURS.sleep(1); // sleep for 1 hour and try again
                continue;
                // real life scenarios should have more complex and reliable solutions
            }
            latestRates.put(base, latestRatesOfCurrentBase);
            //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
        }
        return latestRates;
    }

    private int calculateIterationsLength() {
        long registeredEmailsCount = rateAlertRepository.count();
        return (int) Math.ceil((registeredEmailsCount * 1.0) / chunkSize);
    }

    private void sendAlerts(Map<String, Map<String, Double>> latestRates, int iterations) {
        //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
        for (int startingFrom = 0; startingFrom < iterations; startingFrom += chunkSize) {
            //3. select registered emails in chunks of 50 (could be changed according to need)
            //   - for every chunk process it's registered employees
            List<RateAlert> chunk = rateAlertRepository.findAll(PageRequest.of(startingFrom, chunkSize)).toList();
            for (RateAlert rateAlert : chunk) {
                //4. for each chunk -> send rates to registered emails
                //   Base: XXX
                //   Code | Rate
                //    x   |   x
                //       ...
                Map<String, Double> latestRatesOfCurrentBase = latestRates.getOrDefault(rateAlert.getBase(), Collections.emptyMap());
                Context context = new Context();
                context.setVariable("base", rateAlert.getBase());
                context.setVariable("rates", latestRatesOfCurrentBase);
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(defaultSender);
                msg.setTo(rateAlert.getEmail());
                msg.setSubject(defaultSubject);
                msg.setText(templateEngine.process("mailTemplate", context));
                mailSender.send(msg);
                //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
            }
        }
    }

    private DbConstraintViolationException wrapDataIntegrityViolationException(DataIntegrityViolationException e) {
        String exceptionMessage = e.getMostSpecificCause().getMessage();
        String errorMsg = null;
        if (exceptionMessage != null) {
            String lowerCaseExceptionMessage = exceptionMessage.toLowerCase();
            if (lowerCaseExceptionMessage.contains(RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL)) {
                errorMsg = Messages.EMAIL_ALREADY_EXIST;
            }
        }
        return new DbConstraintViolationException(errorMsg, e);
    }

}
