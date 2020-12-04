package com.practice.application.ratealert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.infrastructure.integration.CurrencyConversionClient;
import com.practice.infrastructure.integration.models.Rate;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class RateAlertJob {

    private final RateAlertRepository rateAlertRepository;

    private final CurrencyConversionClient currencyConversionProvider;

    private final MailSender mailSender;

    private final ITemplateEngine templateEngine;

    private final String defaultSender;

    private final String defaultSubject;

    private final int chunkSize;

    public RateAlertJob(RateAlertRepository rateAlertRepository, CurrencyConversionClient currencyConversionProvider,
                        MailSender mailSender, ITemplateEngine templateEngine,
                        @Value("${via.default-email.sender}") String defaultSender,
                        @Value("${via.default-email.subject}") String defaultSubject,
                        @Value("${chunk-size}") int chunkSize) {
        this.rateAlertRepository = rateAlertRepository;
        this.currencyConversionProvider = currencyConversionProvider;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.defaultSender = defaultSender;
        this.defaultSubject = defaultSubject;
        this.chunkSize = chunkSize;
    }

    @Scheduled(cron = "${via.scheduled-email.rate}")
    @SchedulerLock(name = "TaskScheduler_scheduledTask", lockAtLeastFor = "PT40S", lockAtMostFor = "PT55S")
    public void sendScheduledMailAlert() {
        //1. aggregate bases from DB in List<base:String> -> available distinct bases in DB
        var bases = rateAlertRepository.findAllDistinctBases();
        if (!bases.isEmpty()) {
            //2. for each base make a request to get it's data
            var latestRates = getLatestRates(bases);
            var iterations = calculateIterationsLength();
            sendAlerts(latestRates, iterations);
        }
    }

    // TODO use parallelStreams with ForkJoinPool according to available cpu cores later to increase performance :P
    private Map<String, List<Rate>> getLatestRates(List<String> bases) {
        var latestRates = new HashMap<String, List<Rate>>();
        for (var base : bases) {
            var latestRatesOfCurrentBase = currencyConversionProvider.getLatestRatesByBase(base);
            latestRates.put(base, latestRatesOfCurrentBase);
        }
        return latestRates;
    }

    private int calculateIterationsLength() {
        var registeredEmailsCount = rateAlertRepository.count();
        return (int) Math.ceil((registeredEmailsCount * 1.0) / chunkSize);
    }

    private void sendAlerts(Map<String, List<Rate>> latestRates, int iterations) {
        for (var startingFrom = 0; startingFrom < iterations; startingFrom += chunkSize) {
            //3. select registered emails in chunks of 50 (could be changed according to need)
            //   - for every chunk process it's registered employees
            var chunk = rateAlertRepository.findAll(PageRequest.of(startingFrom, chunkSize)).toList();
            for (RateAlert rateAlert : chunk) {
                //4. for each chunk -> send rates to registered emails
                //   Base: XXX
                //   Code | Rate
                //    x   |   x
                //       ...
                var latestRatesOfCurrentBase = latestRates.get(rateAlert.getBase());
                var context = new Context();
                context.setVariable("base", rateAlert.getBase());
                context.setVariable("rates", latestRatesOfCurrentBase);
                var msg = new SimpleMailMessage();
                msg.setFrom(defaultSender);
                msg.setTo(rateAlert.getEmail());
                msg.setSubject(defaultSubject);
                msg.setText(templateEngine.process("mailTemplate", context));
                mailSender.send(msg);
            }
        }
    }

}
