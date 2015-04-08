package com.boundary.metrics;


import com.boundary.metrics.converter.CountingExtensionFilter;
import com.boundary.metrics.converter.MeteredExtensionFilter;
import com.boundary.metrics.converter.SamplingExtensionFilter;
import com.boundary.metrics.rpc.BoundaryClient;
import com.boundary.metrics.rpc.BoundaryRpcClient;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Background reporter that sends metrics to a Boundary Meter
 */
public class BoundaryReporter extends ScheduledReporter{

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundaryReporter.class);

    private final SamplingExtensionFilter sampling;
    private final CountingExtensionFilter counting;
    private final MeteredExtensionFilter metered;
    private final BoundaryClient client;

    protected BoundaryReporter(Builder builder) {
        super(builder.registry, "boundary-reporter", builder.filter, builder.rateUnit, builder.durationUnit);

        this.sampling = new SamplingExtensionFilter(builder.extensions, builder.prefix);
        this.counting = new CountingExtensionFilter(builder.extensions, builder.prefix);
        this.metered = new MeteredExtensionFilter(builder.extensions, builder.prefix);
        this.client = builder.client;
    }

    private Function<Double, Double> convertRate = new Function<Double, Double>() {
        @Override
        public Double apply(Double input) {
            return convertRate(input);
        }
    };

    private Function<Double, Double> convertDuration = new Function<Double, Double>() {
        @Override
        public Double apply(Double input) {
            return convertDuration(input);
        }
    };

    private Function<Double, Double> noConversion = new Function<Double, Double>() {
        @Override
        public Double apply(Double input) {
            return input;
        }
    };

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        List<Measure> measures = new ArrayList<>();

        addGauges(gauges, measures);
        addCounters(counters, measures);
        addHistograms(histograms, measures);
        addMeters(meters, measures);
        addTimers(timers, measures);

        client.addMeasures(measures);

    }

    private void addTimers(SortedMap<String, Timer> timers, List<Measure> measures) {
        for (Map.Entry<String, Timer> entry: timers.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
            sampling.filter(entry.getKey(), entry.getValue(), convertDuration, measures);
            metered.filter(entry.getKey(), entry.getValue(), convertRate, measures);
        }
    }


    private void addMeters(SortedMap<String, Meter> meters, List<Measure> measures) {
        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
            metered.filter(entry.getKey(), entry.getValue(), convertRate, measures);
        }
    }

    private void addHistograms(SortedMap<String, Histogram> histograms, List<Measure> measures) {
        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
            sampling.filter(entry.getKey(), entry.getValue(), noConversion, measures);
        }
    }

    private void addGauges(SortedMap<String, Gauge> gauges, List<Measure> measures) {
        for (Map.Entry<String, Gauge> entry: gauges.entrySet()) {
            Object val = entry.getValue().getValue();
            if (val instanceof Number) {
                Double vn = ((Number) val).doubleValue();
                if (!(Double.isInfinite(vn) || Double.isNaN(vn))) {
                    measures.add(new Measure(entry.getKey(), vn));
                }
            }
        }
    }

    private void addCounters(SortedMap<String, Counter> counters, List<Measure> measures) {
        for (Map.Entry<String, Counter> entry: counters.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
        }
    }

    public static Builder reporter() {
        return new Builder();
    }


    public static class Builder {

        private MetricRegistry registry;
        private MetricFilter filter = MetricFilter.ALL;
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;

        private BoundaryClient client = BoundaryRpcClient.newInstance();
        private String prefix;
        private Set<MetricExtension> extensions = MetricExtension.ALL;


        public Builder setRegistry(MetricRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder setFilter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder setRateUnit(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder setDurationUnit(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setExtensions(Set<MetricExtension> extensions) {
            this.extensions = extensions;
            return this;
        }

        public Builder setClient(BoundaryClient client) {
            this.client = client;
            return this;
        }

        public BoundaryReporter build() {
            return new BoundaryReporter(this);
        }

    }
}
