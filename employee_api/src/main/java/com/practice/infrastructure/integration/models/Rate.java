package com.practice.infrastructure.integration.models;

import java.util.Objects;

public class Rate {

    private String code;

    private double rate;

    public Rate() {}

    public Rate(String code, double rate) {
        this.code = code;
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Rate)) {
            return false;
        }
        Rate that = (Rate) other;
        return Objects.equals(code, that.code)
            && Objects.equals(rate, that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, rate);
    }

}
