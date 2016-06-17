package com.bmc.truesight.saas.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class IntegrationTest {


    public static void main(String[] args) throws Exception {

        MetricRegistry registry = new MetricRegistry();

        final Counter c = registry.counter(name(IntegrationTest.class.getSimpleName(), "a-counter"));
        final Histogram h = registry.histogram(name(IntegrationTest.class.getSimpleName(), "a-histogram"));
        final Timer t = registry.timer("timer");

        final Meter m = registry.meter("meter");

        Gauge<Double> inverseCounter = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1.0, c.getCount());
            }
        };
        registry.register("test_gauge", inverseCounter);

        Set<MetricExtension> extensions = ImmutableSet.of(
                MetricExtension.Counting.COUNT
                , MetricExtension.Metering.OneMinuteRate
        );

        TrueSightMeterReporter reporter = TrueSightMeterReporter.builder()
                .setDurationUnit(TimeUnit.SECONDS)
                .setRateUnit(TimeUnit.SECONDS)
                .setPrefix("test")
                .setExtensions(extensions)
                .setRegistry(registry)
                .build();

        reporter.start(1, TimeUnit.SECONDS);

        for (int i = 0; i < 10; i++) {
            try (Timer.Context ignored = t.time()) {
                c.inc();
                h.update(i);
                m.mark();
            }
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

    }

}
