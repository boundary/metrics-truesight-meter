package com.bmc.truesight.saas.metrics.filter;

import com.bmc.truesight.saas.metrics.Fn;
import com.bmc.truesight.saas.metrics.NameFactory;
import com.bmc.truesight.saas.meter.client.model.Measure;
import com.bmc.truesight.saas.metrics.MetricExtension;
import com.codahale.metrics.Counting;

import java.util.List;
import java.util.Set;

public class CountingExtFilter implements Fn.ExtFilter<Counting> {
    private final boolean includeCount;
    private final NameFactory nameFactory;

    public CountingExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;

        boolean includeCount = false;
        for (MetricExtension extension : extensions) {
            if (extension instanceof MetricExtension.Counting) {
                includeCount = true;
            }
        }
        this.includeCount = includeCount;
    }

    @Override
    public void filter(String name, com.codahale.metrics.Counting counting, Fn.RateConverter ignored, List<Measure> list) {
        if (includeCount) {
            list.add(Measure.of(nameFactory.name(name, MetricExtension.Counting.COUNT),
                                counting.getCount(),
                                nameFactory.source()));
        }
    }
}
