package com.boundary.metrics;

import com.boundary.meter.client.model.Measure;

import java.util.List;

public interface Fn {

    @FunctionalInterface
    public interface RateConverter {
        Double convert(Double value);
    }

    @FunctionalInterface
    public interface GetValue<T> {
        Double getValue(T source);
    }

    @FunctionalInterface
    public interface ExtFilter<T> {
        void filter(String name, T toConvert, RateConverter converter, List<Measure> list);
    }
}