package com.boundary.metrics.filter;

import com.boundary.meter.client.model.Measure;
import com.boundary.metrics.Fn;
import com.boundary.metrics.MetricExtension;
import com.boundary.metrics.NameFactory;

import java.util.List;
import java.util.Set;

public class CountingExtFilter implements Fn.ExtFilter<com.codahale.metrics.Counting> {
    private final boolean includeCount;
    private final NameFactory nameFactory;

    public CountingExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;

        boolean includeCount = false;
        for(MetricExtension extension: extensions) {
            if (extension instanceof MetricExtension.Counting) {
                includeCount = true;
            }
        }
        this.includeCount = includeCount;
    }

    @Override
    public void filter(String name, com.codahale.metrics.Counting counting, Fn.RateConverter ignored, List<Measure> list) {

        if (includeCount) {
            list.add(new Measure(nameFactory.name(name, MetricExtension.Counting.COUNT), counting.getCount()));
        }
    }
}
