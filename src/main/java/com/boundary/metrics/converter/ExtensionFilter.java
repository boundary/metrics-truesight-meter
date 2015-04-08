package com.boundary.metrics.converter;

import com.boundary.metrics.Measure;
import com.google.common.base.Function;

import java.util.List;

public interface ExtensionFilter<T> {
    void filter(String key, T toConvert, Function<Double, Double> converter, List<Measure> list);
}
