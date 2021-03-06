package com.bmc.truesight.saas.metrics;

import com.bmc.truesight.saas.meter.client.model.Measure;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TrueSightMeterReporterTest {

    private TestCapturingClient client = new TestCapturingClient();
    private MetricRegistry registry = new MetricRegistry();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void testFilterMetrics() throws InterruptedException, IOException {

        final Counter c = registry.counter(name(getClass().getSimpleName(), "test-counter"));
        final Histogram h = registry.histogram(name(getClass().getSimpleName(), "test-histogram"));
        final Timer t = registry.timer("test-timer");
        final Meter m = registry.meter("test-meter");

        Set<MetricExtension> extensions = Sets.newHashSet();
        extensions.add(MetricExtension.Counting.COUNT);
        extensions.add(MetricExtension.Sampling.Median);
        extensions.add(MetricExtension.Metering.OneMinuteRate);

        Gauge<Double> inverseCounter = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1.0, c.getCount());
            }
        };
        registry.register("test-gauge", inverseCounter);

        String prefix = "test";
        TrueSightMeterReporter reporter = TrueSightMeterReporter.builder()
                                                                .setDurationUnit(TimeUnit.SECONDS)
                                                                .setRateUnit(TimeUnit.SECONDS)
                                                                .setClient(client)
                                                                .setExtensions(extensions)
                                                                .setPrefix(prefix)
                                                                .setRegistry(registry)
                                                                .build();
        reporter.start(1, TimeUnit.SECONDS);


        executorService.submit((Runnable) () -> {

            for (int i = 0; i < 10; i++) {
                try (Timer.Context ignored = t.time()) {
                    c.inc();
                    h.update(i);
                    m.mark();
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
        });

        await().atMost(15, TimeUnit.SECONDS).until(() -> client.getCaptured().size(), is(10));

        for (Iterable<Measure> measures : client.getCaptured()) {
            // 1x gauge
            // 4x counter (timer,meter,histogram and counter)
            // 2x mean (timer, meter)
            // 2x median (timer, histogram)
            assertThat(Iterables.size(measures), is(9));

            for (Measure measure : measures) {
                assertTrue(measure.name().startsWith(prefix));
            }
        }

        reporter.close();
        assertThat(client.isClosed(), is(true));
    }

    @Test
    public void testFilterMetricsWithSource() throws InterruptedException, IOException {

        final Counter c = registry.counter(name(getClass().getSimpleName(), "test-counter"));
        final Histogram h = registry.histogram(name(getClass().getSimpleName(), "test-histogram"));
        final Timer t = registry.timer("test-timer");
        final Meter m = registry.meter("test-meter");

        Set<MetricExtension> extensions = Sets.newHashSet();
        extensions.add(MetricExtension.Counting.COUNT);
        extensions.add(MetricExtension.Sampling.Median);
        extensions.add(MetricExtension.Metering.OneMinuteRate);

        Gauge<Double> inverseCounter = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1.0, c.getCount());
            }
        };
        registry.register("test-gauge", inverseCounter);

        String prefix = "test";
        TrueSightMeterReporter reporter = TrueSightMeterReporter.builder()
                                                                .setDurationUnit(TimeUnit.SECONDS)
                                                                .setRateUnit(TimeUnit.SECONDS)
                                                                .setClient(client)
                                                                .setExtensions(extensions)
                                                                .setPrefix(prefix)
                                                                .setSource("Vulcan")
                                                                .setRegistry(registry)
                                                                .build();
        reporter.start(1, TimeUnit.SECONDS);


        executorService.submit((Runnable) () -> {

            for (int i = 0; i < 10; i++) {
                try (Timer.Context ignored = t.time()) {
                    c.inc();
                    h.update(i);
                    m.mark();
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
        });

        await().atMost(15, TimeUnit.SECONDS).until(() -> client.getCaptured().size(), is(10));

        for (Iterable<Measure> measures : client.getCaptured()) {
            // 1x gauge
            // 4x counter (timer,meter,histogram and counter)
            // 2x mean (timer, meter)
            // 2x median (timer, histogram)
            assertThat(Iterables.size(measures), is(9));

            for (Measure measure : measures) {
                assertTrue(measure.name().startsWith(prefix));
                assertEquals(measure.source().orElse(null), "Vulcan");
            }
        }

        reporter.close();
        assertThat(client.isClosed(), is(true));
    }
}