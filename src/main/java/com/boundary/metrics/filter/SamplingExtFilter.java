package com.boundary.metrics.filter;

import com.boundary.meter.client.model.Measure;
import com.boundary.metrics.Fn;
import com.boundary.metrics.MetricExtension;
import com.boundary.metrics.NameFactory;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.boundary.metrics.MetricExtension.Sampling.*;

public class SamplingExtFilter implements Fn.ExtFilter<com.codahale.metrics.Sampling> {

    private final ImmutableSet<Sampling> extensions;
    private final NameFactory nameFactory;
    private final boolean includeAny;

    public SamplingExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;

        ImmutableSet.Builder<Sampling> samplingExtensionBuilder = ImmutableSet.builder();
        for (MetricExtension ext: extensions) {
            if (ext instanceof Sampling) {
                samplingExtensionBuilder.add((Sampling) ext);
            }
        }
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
            list.add(new Measure(nameFactory.name(name, ext), converter.convert(ext.getValue(sn))));
        });

    }
}
