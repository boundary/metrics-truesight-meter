package com.boundary.metrics.filter;

import com.boundary.metrics.Fn;
import com.boundary.metrics.Measure;
import com.boundary.metrics.MetricExtension;
import com.boundary.metrics.NameFactory;
import com.codahale.metrics.Metered;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.boundary.metrics.MetricExtension.Metering.*;


public class MeteredExtFilter implements Fn.ExtFilter<Metered> {

    private final ImmutableSet<Metering> extensions;
    private final NameFactory nameFactory;

    public MeteredExtFilter(Set<MetricExtension> extensions, NameFactory nameFactory) {
        this.nameFactory = nameFactory;


        ImmutableSet.Builder<Metering> meteredExtensions = ImmutableSet.builder();
        for (MetricExtension ext: extensions) {
            if (ext instanceof Metering) {
                meteredExtensions.add((Metering) ext);
            }
        }
        this.extensions = meteredExtensions.build();

    }

    @Override
    public void filter(String name, Metered metered, Fn.RateConverter converter, List<Measure> list) {

        extensions.forEach(ext -> {
            list.add(new Measure(nameFactory.name(name, ext), converter.convert(ext.getValue(metered))));
        });

    }

}
