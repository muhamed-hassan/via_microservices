package com.practice.infrastructure.integration.models;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.practice.interfaces.rest.serializers.StatisticsOfRatesSerializer;

@JsonSerialize(using = StatisticsOfRatesSerializer.class)
public class StatisticsOfRates {

    private double lowest;

    private double highest;

    public StatisticsOfRates() {}

    public StatisticsOfRates(double lowest, double highest) {
        this.lowest = lowest;
        this.highest = highest;
    }

    public double getLowest() {
        return lowest;
    }

    public void setLowest(double lowest) {
        this.lowest = lowest;
    }

    public double getHighest() {
        return highest;
    }

    public void setHighest(double highest) {
        this.highest = highest;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StatisticsOfRates)) {
            return false;
        }
        StatisticsOfRates that = (StatisticsOfRates) other;
        return Objects.equals(that.lowest, lowest) &&
                Objects.equals(that.highest, highest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowest, highest);
    }

}
