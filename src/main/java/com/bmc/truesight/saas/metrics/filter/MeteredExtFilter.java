package com.bmc.truesight.saas.metrics.filter;

import com.bmc.truesight.saas.metrics.Fn;
import com.bmc.truesight.saas.metrics.MetricExtension;
import com.bmc.truesight.saas.metrics.NameFactory;
import com.bmc.truesight.saas.meter.client.model.Measure;
import com.codahale.metrics.Metered;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;


public class MeteredExtFilter implements Fn.ExtFilter<Metered> {

    private final ImmutableSet<MetricExtension.Metering> extensions;
    private final NameFactory nameFactory;

    public MeteredExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;

        ImmutableSet.Builder<MetricExtension.Metering> meteredExtensions = ImmutableSet.builder();

        extensions
                .stream()
                .filter(ext -> ext instanceof MetricExtension.Metering)
                .forEach(ext -> meteredExtensions.add((MetricExtension.Metering) ext));

        this.extensions = meteredExtensions.build();
    }

    @Override
    public void filter(String name, Metered metered, Fn.RateConverter converter, List<Measure> list) {
        extensions.forEach(ext -> {
            list.add(Measure.of(nameFactory.name(name, ext),
                                converter.convert(ext.getValue(metered)),
                                nameFactory.source()));
        });
    }
}
