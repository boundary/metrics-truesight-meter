package com.bmc.truesight.saas.metrics;


import com.bmc.truesight.saas.meter.client.rpc.TruesightRpcClient;
import com.bmc.truesight.saas.metrics.filter.CountingExtFilter;
import com.bmc.truesight.saas.metrics.filter.MeteredExtFilter;
import com.bmc.truesight.saas.meter.client.TruesightMeterClient;
import com.bmc.truesight.saas.meter.client.model.Measure;
import com.bmc.truesight.saas.meter.client.rpc.TruesightMeterRpcClientConfig;
import com.bmc.truesight.saas.meter.client.rpc.TruesightMeterRpcClientConfig;
import com.bmc.truesight.saas.metrics.filter.SamplingExtFilter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Background reporter that sends metrics to a Truesight Meter
 */
public class TrueSightMeterReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrueSightMeterReporter.class);

    private final SamplingExtFilter sampling;
    private final CountingExtFilter counting;
    private final MeteredExtFilter metered;
    private final TruesightMeterClient client;
    private final NameFactory nameFactory;


    protected TrueSightMeterReporter(Builder builder) {
        super(builder.registry, "truesight-meter-reporter", builder.filter, builder.rateUnit, builder.durationUnit);

        this.nameFactory = new NameFactory(builder.prefix, builder.masks, builder.source);

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
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
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
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            Object val = entry.getValue().getValue();
            if (val instanceof Number) {
                Double vn = ((Number) val).doubleValue();
                if (!(Double.isInfinite(vn) || Double.isNaN(vn))) {
                    measures.add(Measure.of(nameFactory.name(entry.getKey()), vn, nameFactory.source()));
                }
            }
        }
    }

    private void addCounters(SortedMap<String, Counter> counters, List<Measure> measures) {
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            counting.filter(entry.getKey(), entry.getValue(), noConversion, measures);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            client.close();
        } catch (Exception e) {
            LOGGER.error("Error closing Truesight meter client", e);
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

        private TruesightMeterClient client;
        private String prefix = "";
        private String source = "";
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

        public Builder setSource(String source) {
            this.source = source;
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

        public Builder setClient(TruesightMeterClient client) {
            this.client = client;
            return this;
        }

        public TrueSightMeterReporter build() {

            checkNotNull(registry);
            checkNotNull(filter);
            checkNotNull(rateUnit);
            checkNotNull(durationUnit);
            checkNotNull(meter);
            checkNotNull(prefix);
            checkNotNull(source);
            checkNotNull(extensions);

            if (client == null) {
                TruesightMeterRpcClientConfig config = new TruesightMeterRpcClientConfig();
                config.setMeter(meter);
                TruesightRpcClient _client = null;
                try {
                    _client = new TruesightRpcClient(config);
                    _client.connect();
                } catch (Exception e) {
                    LOGGER.error("Unable to connect to Truesight meter at " + meter.toString(), e);
                }
                client = _client;
            }

            return new TrueSightMeterReporter(this);
        }

    }
}
