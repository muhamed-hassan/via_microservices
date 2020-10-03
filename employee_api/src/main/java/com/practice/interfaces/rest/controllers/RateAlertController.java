package com.practice.interfaces.rest.controllers;

import java.net.HttpURLConnection;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practice.application.ratealert.RateAlertService;
import com.practice.domain.ratealert.RateAlert;
import com.practice.interfaces.rest.assemblers.EntityAssembler;
import com.practice.interfaces.rest.dtos.RateAlertDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Rate Alert API")
@RestController
@RequestMapping("v1/alerts")
@Validated
public class RateAlertController {

    private final RateAlertService rateAlertService;

    private final EntityAssembler entityAssembler;

    public RateAlertController(RateAlertService rateAlertService, EntityAssembler entityAssembler) {
        this.rateAlertService = rateAlertService;
        this.entityAssembler = entityAssembler;
    }

    @ApiOperation("Register for scheduled mail alerts")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "The request of scheduled alert is registered and will be processed later"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Rate alert payload contains invalid value"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to accept the request of scheduled alert")
    })
    @PostMapping("rates")
    public ResponseEntity<Void> registerForScheduledMailAlert(@RequestBody @Valid RateAlertDto rateAlertDto) {
        rateAlertService.registerForScheduledMailAlert(entityAssembler.toEntity(rateAlertDto, RateAlert.class));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
