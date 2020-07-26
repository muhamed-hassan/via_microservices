package com.practice.services.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import com.practice.exceptions.handlers.EmployeeServiceErrorHandler;
import com.practice.persistence.entities.RateAlertEntity;
import com.practice.persistence.repositories.RateAlertRepository;
import com.practice.services.AlertSchedularService;
import com.practice.services.CurrencyConversionService;
import com.practice.transfomers.EntityTransformer;
import com.practice.web.dtos.RateAlertDto;

@Service
public class AlertSchedularServiceImpl implements AlertSchedularService {

    private Logger logger = LoggerFactory.getLogger(AlertSchedularServiceImpl.class);

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private RateAlertRepository rateAlertRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ITemplateEngine templateEngine;

    @Autowired
    private EntityTransformer entityTransformer;

    @Autowired
    private EmployeeServiceErrorHandler employeeServiceErrorHandler;

    @Value("${via.default-email.sender}")
    private String defaultSender;

    @Value("${via.default-email.subject}")
    private String defaultSubject;

    @Value("${chunk-size}")
    private int chunkSize;

    // assuming employee can register only for one rate
    @Transactional
    @Override
    public void registerForScheduledMailAlert(final RateAlertDto rateAlertDto) {
        try {
            rateAlertRepository.save(entityTransformer.toEntity(rateAlertDto, RateAlertEntity.class));
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMostSpecificCause().getMessage());
            employeeServiceErrorHandler.handleDataIntegrityViolationException(e);
        }
    }

    @Scheduled(cron = "${via.scheduled-email.rate}")
    @Override
    public void sendScheduledMailAlert() throws InterruptedException {
        //1. aggregate bases from DB in List<base:String> -> available distinct bases in DB
        List<String> bases = rateAlertRepository.getBases();
        if (!bases.isEmpty()) {
            //2. for each base make a request to get it's data
            Map<String, Map<String, Double>> latestRates = getLatestRates(bases);

            int iterations = calculateIterationsLength();

            sendAlerts(latestRates, iterations);
        }
    }

    private Map<String, Map<String, Double>> getLatestRates(final List<String> bases) throws InterruptedException {
        Map<String, Map<String, Double>> latestRates = new HashMap<>();
        int cursor;
        for (cursor = 0; cursor < bases.size(); cursor++) {
            String base = bases.get(cursor);
            Map<String, Double> latestRatesOfCurrentBase = currencyConversionService.getLatestRatesByBase(base);
            if (latestRatesOfCurrentBase.isEmpty()) { // means 3rd part API is down (can't connect to it properly)
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

    private void sendAlerts(final Map<String, Map<String, Double>> latestRates, final int iterations) {
        //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
        for (int startingFrom = 0; startingFrom < iterations; startingFrom += chunkSize) {
            //3. select registered emails in chunks of 50 (could be changed according to need)
            //   - for every chunk process it's registered employees
            List<RateAlertEntity> chunk = rateAlertRepository.findAll(PageRequest.of(startingFrom, chunkSize)).toList();
            for (RateAlertEntity rateAlert : chunk) {
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

}
