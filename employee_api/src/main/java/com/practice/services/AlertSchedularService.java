package com.practice.services;

import com.practice.web.dtos.RateAlertDto;

public interface AlertSchedularService {



    void sendScheduledMailAlert() throws InterruptedException;

}
