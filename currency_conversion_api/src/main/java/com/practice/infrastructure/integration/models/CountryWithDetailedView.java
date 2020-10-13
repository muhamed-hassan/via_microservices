package com.practice.infrastructure.integration.models;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.practice.interfaces.rest.serializers.CountryWithDetailedViewSerializer;

@JsonSerialize(using = CountryWithDetailedViewSerializer.class)
public class CountryWithDetailedView {

    private String name;

    private List<Currency> currencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

}
