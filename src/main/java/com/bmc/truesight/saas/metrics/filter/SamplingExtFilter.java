package com.bmc.truesight.saas.metrics.filter;

import com.bmc.truesight.saas.metrics.Fn;
import com.bmc.truesight.saas.metrics.MetricExtension;
import com.bmc.truesight.saas.metrics.NameFactory;
import com.bmc.truesight.saas.meter.client.model.Measure;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class SamplingExtFilter implements Fn.ExtFilter<Sampling> {

    private final ImmutableSet<MetricExtension.Sampling> extensions;
    private final NameFactory nameFactory;
    private final boolean includeAny;

    public SamplingExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;

        ImmutableSet.Builder<MetricExtension.Sampling> samplingExtensionBuilder = ImmutableSet.builder();
        extensions
                .stream()
                .filter(ext -> ext instanceof MetricExtension.Sampling)
                .forEach(ext -> {
                    samplingExtensionBuilder.add((MetricExtension.Sampling) ext);
                });

        this.extensions = samplingExtensionBuilder.build();
        this.includeAny = !extensions.isEmpty();
    }

    @Override
    public void filter(String name, com.codahale.metrics.Sampling toConvert, Fn.RateConverter converter, List<Measure> list) {

        if (!includeAny) {
            return;
        }

        final Snapshot sn = toConvert.getSnapshot();

        extensions.forEach(ext -> {
            list.add(Measure.of(nameFactory.name(name, ext),
                                converter.convert(ext.getValue(sn)),
                                nameFactory.source()));
        });
    }
}
