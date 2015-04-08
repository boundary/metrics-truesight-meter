package com.boundary.metrics.converter;

import com.boundary.metrics.Measure;
import com.boundary.metrics.MetricExtension;
import com.codahale.metrics.Counting;
import com.google.common.base.Function;

import java.util.List;
import java.util.Set;

import static com.boundary.metrics.MetricExtension.Joiner.join;

public class CountingExtensionFilter implements ExtensionFilter<Counting> {
    private final boolean includeCount;
    private final String prefix;

    public CountingExtensionFilter(Set<MetricExtension> extensions, String prefix) {
        this.prefix = prefix;

        boolean includeCount = false;
        for(MetricExtension extension: extensions) {
            if (extension instanceof MetricExtension.CountingExtension) {
                includeCount = true;
            }
        }
        this.includeCount = includeCount;
    }

    @Override
    public void filter(String key, Counting counting, Function<Double, Double> ignored, List<Measure> list) {
        if (includeCount) {
            list.add(new Measure(join(prefix, key, MetricExtension.CountingExtension.COUNT), counting.getCount()));
        }
    }
}
