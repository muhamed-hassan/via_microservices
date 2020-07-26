package com.practice.transfomers;

import static java.util.stream.StreamSupport.stream;
import static java.util.stream.Collectors.toMap;

import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class CurrncyConversionTransformer {

    public <T, K, V> Map<K, V> transform(final Spliterator<T> spliterator, final Function<T, K> keyMapper, final Function<T, V> valueMapper) {
        return stream(spliterator, false)
                    .collect(toMap(keyMapper, valueMapper));
    }

    public <T, A, V, R> R transform(final Spliterator<T> spliterator, final Function<T, V> valueMapper, final Collector<V, A, R> collector) {
        return stream(spliterator, false)
                    .map(valueMapper)
                    .collect(collector);
    }

    public <T, V> DoubleSummaryStatistics getStatistics(final Stream<T> elementsStream, final Function<T, V> valueMapper) {
        return elementsStream
                    .map(valueMapper)
                    .mapToDouble(value -> ((Double) value).doubleValue())
                    .summaryStatistics();
    }

}
