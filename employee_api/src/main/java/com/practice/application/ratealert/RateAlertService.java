package com.practice.application.ratealert;

import com.practice.domain.ratealert.RateAlert;

public interface RateAlertService {

    void registerForScheduledMailAlert(RateAlert rateAlert);

    void sendScheduledMailAlert() throws InterruptedException;

}
