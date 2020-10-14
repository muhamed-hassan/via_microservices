package com.practice.interfaces.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.practice.infrastructure.integration.models.StatisticsOfRates;

public class StatisticsOfRatesSerializer extends StdSerializer<StatisticsOfRates> {

    public StatisticsOfRatesSerializer() {
        this(null);
    }

    public StatisticsOfRatesSerializer(Class<StatisticsOfRates> t) {
        super(t);
    }

    @Override
    public void serialize(StatisticsOfRates value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (value.getLowest() != 0.0 && value.getHighest() != 0.0) {
            jgen.writeNumberField("lowest", value.getLowest());
            jgen.writeNumberField("highest", value.getHighest());
        }
        jgen.writeEndObject();
    }

}
