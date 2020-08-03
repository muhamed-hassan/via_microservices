package com.practice.services.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.practice.integration.CurrencyConversionProvider;
import com.practice.persistence.entities.RateAlert;
import com.practice.persistence.repositories.RateAlertRepository;
import com.practice.services.AlertSchedularService;

@Service
public class AlertSchedularServiceImpl implements AlertSchedularService {

    @Autowired
    private RateAlertRepository rateAlertRepository;

    @Autowired
    private CurrencyConversionProvider currencyConversionProvider;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ITemplateEngine templateEngine;

    @Value("${via.default-email.sender}")
    private String defaultSender;

    @Value("${via.default-email.subject}")
    private String defaultSubject;

    @Value("${chunk-size}")
    private int chunkSize;

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
            Map<String, Double> latestRatesOfCurrentBase = currencyConversionProvider.getLatestRatesByBase(base);
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

}
