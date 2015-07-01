package com.boundary.metrics;


import com.boundary.metrics.filter.CountingExtFilter;
import com.boundary.metrics.filter.MeteredExtFilter;
import com.boundary.metrics.filter.SamplingExtFilter;
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
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Background reporter that sends metrics to a Boundary Meter
 */
public class BoundaryReporter extends ScheduledReporter{

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundaryReporter.class);

    private final SamplingExtFilter sampling;
    private final CountingExtFilter counting;
    private final MeteredExtFilter metered;
    private final BoundaryClient client;
    private final NameFactory nameFactory;


    protected BoundaryReporter(Builder builder) {
        super(builder.registry, "boundary-reporter", builder.filter, builder.rateUnit, builder.durationUnit);

        this.nameFactory = new NameFactory(builder.prefix, builder.masks);

        this.sampling = new SamplingExtFilter(builder.extensions, nameFactory);
        this.counting = new CountingExtFilter(builder.extensions, nameFactory);
        this.metered = new MeteredExtFilter(builder.extensions, nameFactory);
        this.client = builder.client;
    }

    private Fn.RateConverter convertRate = this::convertRate;
    private Fn.RateConverter convertDuration = this::convertDuration;
    private Fn.RateConverter noConversion = input -> input;

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
                    measures.add(new Measure(nameFactory.name(entry.getKey()), vn));
                }
            }
        }
    }

    private void addCounters(SortedMap<String, Counter> counters, List<Measure> measures) {
        for (Map.Entry<String, Counter> entry: counters.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            client.close();
        } catch (IOException e) {
            LOGGER.error("Error closing boundary client", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private MetricRegistry registry;
        private MetricFilter filter = MetricFilter.ALL;
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
        private HostAndPort meter = HostAndPort.fromParts("localhost", 9192);

        private BoundaryClient client;
        private String prefix = "";
        private Set<MetricExtension> extensions = MetricExtension.ALL;
        private List<String> masks = ImmutableList.of();


        public Builder setMeter(HostAndPort meter) {
            this.meter = meter;
            return this;
        }

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

        public Builder setMasks(List<String> masks) {
            this.masks = masks;
            return this;
        }

        public Builder setClient(BoundaryClient client) {
            this.client = client;
            return this;
        }

        public BoundaryReporter build() {

            checkNotNull(registry);
            checkNotNull(filter);
            checkNotNull(rateUnit);
            checkNotNull(durationUnit);
            checkNotNull(meter);
            checkNotNull(prefix);
            checkNotNull(extensions);

            if (client == null) {
                BoundaryRpcClient c = BoundaryRpcClient.newInstance(meter);
                try {
                    c.start();
                } catch (IOException e) {
                    LOGGER.error("Unable to connect to boundary meter at " + meter.toString(), e);
                }
                this.client = c;
            }

            return new BoundaryReporter(this);
        }

    }
}
