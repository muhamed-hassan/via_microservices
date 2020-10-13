package com.practice.interfaces.rest.serializers;

import java.io.IOException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;
import com.practice.infrastructure.integration.models.Currency;

public class CountryWithDetailedViewSerializer extends StdSerializer<CountryWithDetailedView> {

    public CountryWithDetailedViewSerializer() {
        this(null);
    }

    public CountryWithDetailedViewSerializer(Class<CountryWithDetailedView> t) {
        super(t);
    }
    
    @Override
    public void serialize(CountryWithDetailedView value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("name", value.getName());
        jgen.writeFieldName("currencies");
        jgen.writeArray(value.getCurrencies()
                                .stream()
                                .map(Currency::getCode)
                                .collect(Collectors.toList())
                                .toArray(new String[value.getCurrencies().size()]),
                    0, value.getCurrencies().size());
        jgen.writeEndObject();
    }

}
