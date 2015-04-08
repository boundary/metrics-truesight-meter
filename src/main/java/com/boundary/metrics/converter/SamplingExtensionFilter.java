package com.boundary.metrics.converter;

import com.boundary.metrics.Measure;
import com.boundary.metrics.MetricExtension;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.boundary.metrics.MetricExtension.Joiner.join;
import static com.boundary.metrics.MetricExtension.SamplingExtension.*;

public class SamplingExtensionFilter implements ExtensionFilter<Sampling> {

    private final ImmutableSet<SamplingExtension> extensions;
    private final String prefix;
    private final boolean includeAny;

    public SamplingExtensionFilter(Set<MetricExtension> extensions, String prefix) {
        this.prefix = prefix;

        ImmutableSet.Builder<SamplingExtension> samplingExtensionBuilder = ImmutableSet.builder();
        for (MetricExtension ext: extensions) {
            if (ext instanceof SamplingExtension) {
                samplingExtensionBuilder.add((SamplingExtension) ext);
            }
        }
        this.extensions = samplingExtensionBuilder.build();
        this.includeAny = !extensions.isEmpty();
    }

    @Override
    public void filter(String key, Sampling toConvert, Function<Double, Double> conversion, List<Measure> list) {

        if (!includeAny) {
            return;
        }

        final Snapshot sn = toConvert.getSnapshot();

        for (SamplingExtension extension: extensions) {
            list.add(new Measure(join(prefix, key, extension), conversion.apply(extension.getValue(sn))));
        }


    }
}
