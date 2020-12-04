package com.practice.application.ratealert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.LockModeType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.practice.application.shared.ServiceExceptionHandler;
import com.practice.domain.ratealert.RateAlert;
import com.practice.domain.ratealert.RateAlertRepository;
import com.practice.infrastructure.integration.CurrencyConversionClient;
import com.practice.infrastructure.integration.models.Rate;

@Service
public class RateAlertServiceImpl implements RateAlertService {

    private final RateAlertRepository rateAlertRepository;

    private final CurrencyConversionClient currencyConversionProvider;

    private final MailSender mailSender;

    private final ITemplateEngine templateEngine;

    private final ServiceExceptionHandler serviceExceptionHandler;

    private final String defaultSender;

    private final String defaultSubject;

    private final int chunkSize;

    public RateAlertServiceImpl(RateAlertRepository rateAlertRepository, CurrencyConversionClient currencyConversionProvider,
                                    MailSender mailSender, ITemplateEngine templateEngine, ServiceExceptionHandler serviceExceptionHandler,
                                    @Value("${via.default-email.sender}") String defaultSender,
                                    @Value("${via.default-email.subject}") String defaultSubject,
                                    @Value("${chunk-size}") int chunkSize) {
        this.rateAlertRepository = rateAlertRepository;
        this.currencyConversionProvider = currencyConversionProvider;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.serviceExceptionHandler = serviceExceptionHandler;
        this.defaultSender = defaultSender;
        this.defaultSubject = defaultSubject;
        this.chunkSize = chunkSize;
    }

    @Transactional
    @Override
    public void registerForScheduledMailAlert(RateAlert rateAlert) {
        try {
            rateAlert.setLastSent(LocalDateTime.now());
            rateAlertRepository.save(rateAlert);
        } catch (DataIntegrityViolationException e) {
            throw serviceExceptionHandler.wrapDataIntegrityViolationException(e, RateAlert.class);
        }
    }

//    @Lock(LockModeType.WRITE)
    @Transactional//(isolation = Isolation.DEFAULT)
    @Scheduled(cron = "${via.scheduled-email.rate}")
    @Override
    public void sendScheduledMailAlert() throws InterruptedException {
        //1. aggregate bases from DB in List<base:String> -> available distinct bases in DB
        var bases = rateAlertRepository.findAllDistinctBases();
        if (!bases.isEmpty()) {
            //2. for each base make a request to get it's data
            var latestRates = getLatestRates(bases);
            var iterations = calculateIterationsLength();
            sendAlerts(latestRates, iterations);
        }
    }

    private Map<String, List<Rate>> getLatestRates(List<String> bases) throws InterruptedException {
        var latestRates = new HashMap<String, List<Rate>>();
        for (var cursor = 0; cursor < bases.size(); cursor++) {
            var base = bases.get(cursor);
            var latestRatesOfCurrentBase = currencyConversionProvider.getLatestRatesByBase(base);
            if (latestRatesOfCurrentBase.isEmpty()) { // means that currency-conversion-api API is down
                cursor--; // move the cursor back 1 step, to re-try the failed request
                //TimeUnit.HOURS.sleep(1); // sleep for 1 hour and try again
                TimeUnit.SECONDS.sleep(55);
                continue;
                // real life scenarios should have more complex and reliable solutions
            }
            latestRates.put(base, latestRatesOfCurrentBase);
            //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
        }
        return latestRates;
    }

    private int calculateIterationsLength() {
        var registeredEmailsCount = rateAlertRepository.count();
        return (int) Math.ceil((registeredEmailsCount * 1.0) / chunkSize);
    }

    private void sendAlerts(Map<String, List<Rate>> latestRates, int iterations) {
        //TimeUnit.SECONDS.sleep(10); //make 10 secs diff between each request (could be changed according to need)
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
                var latestRatesOfCurrentBase = latestRates.getOrDefault(rateAlert.getBase(), Collections.emptyList());
                var context = new Context();
                context.setVariable("base", rateAlert.getBase());
                context.setVariable("rates", latestRatesOfCurrentBase);
                var msg = new SimpleMailMessage();
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
