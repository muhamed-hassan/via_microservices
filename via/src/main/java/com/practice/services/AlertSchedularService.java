package com.practice.services;

import com.practice.web.dtos.RateAlertDto;

public interface AlertSchedularService {

    void registerForScheduledMailAlert(RateAlertDto rateAlertDto);

    void sendScheduledMailAlert() throws InterruptedException;

}
