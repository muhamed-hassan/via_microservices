package com.practice.interfaces.rest.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.practice.infrastructure.integration.models.Rates;

public class RatesSerializer extends StdSerializer<Rates> {

    public RatesSerializer() {
        this(null);
    }

    public RatesSerializer(Class<Rates> t) {
        super(t);
    }

    @Override
    public void serialize(Rates value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        var rates = value.getRates();
        for (var entry : rates.entrySet()) {
            jgen.writeStartObject();
            jgen.writeStringField("code", entry.getKey());
            jgen.writeNumberField("rate", entry.getValue());
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }

}
