package com.boundary.metrics.converter;

import com.boundary.metrics.Measure;
import com.boundary.metrics.MetricExtension;
import com.codahale.metrics.Metered;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.boundary.metrics.MetricExtension.Joiner.join;
import static com.boundary.metrics.MetricExtension.MeteredExtension.*;


public class MeteredExtensionFilter implements ExtensionFilter<Metered> {

    private final ImmutableSet<MeteredExtension> extensions;
    private final String prefix;

    public MeteredExtensionFilter(Set<MetricExtension> extensions, String prefix) {
        this.prefix = prefix;


        ImmutableSet.Builder<MeteredExtension> meteredExtensions = ImmutableSet.builder();
        for (MetricExtension ext: extensions) {
            if (ext instanceof MeteredExtension) {
                meteredExtensions.add((MeteredExtension) ext);
            }
        }
        this.extensions = meteredExtensions.build();

    }

    @Override
    public void filter(String key, Metered metered, Function<Double, Double> converter, List<Measure> list) {

        for(MeteredExtension extension : extensions) {
            list.add(new Measure(join(prefix, key, MEANRATE), converter.apply(extension.getValue(metered))));

        }
    }

}
