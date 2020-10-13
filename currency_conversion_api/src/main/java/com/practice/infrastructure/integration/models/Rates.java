package com.practice.infrastructure.integration.models;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.practice.interfaces.rest.serializers.RatesSerializer;

@JsonSerialize(using = RatesSerializer.class)
@JsonRootName("rates")
public class Rates {

    private Map<String, Double> rates;

    public Rates() {}

    public Rates(Map<String, Double> rates) {
        this.rates = rates;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Rates)) {
            return false;
        }
        Rates that = (Rates) other;
        return Objects.equals(rates, that.rates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rates);
    }

}
