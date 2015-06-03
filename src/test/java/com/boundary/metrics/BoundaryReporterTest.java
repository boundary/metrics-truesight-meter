package com.boundary.metrics;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BoundaryReporterTest {

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
        extensions.add(MetricExtension.CountingExtension.COUNT);
        extensions.add(MetricExtension.SamplingExtension.MEDIAN);
        extensions.add(MetricExtension.MeteredExtension.ONEMRATE);


        Gauge<Double> inverseCounter = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1.0, c.getCount());
            }
        };
        registry.register("test-gauge", inverseCounter);

        String prefix = "test";
        BoundaryReporter reporter = BoundaryReporter.reporter()
                .setDurationUnit(TimeUnit.SECONDS)
                .setRateUnit(TimeUnit.SECONDS)
                .setClient(client)
                .setExtensions(extensions)
                .setPrefix(prefix)
                .setRegistry(registry)
                .build();

        reporter.start(1, TimeUnit.SECONDS);


        executorService.submit(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 10; i++) {
                    try (Timer.Context ignored = t.time()) {
                        c.inc();
                        h.update(i);
                        m.mark();
                    }

                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                }
            }
        });


        await().atMost(15, TimeUnit.SECONDS).until(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return client.getCaptured().size();
            }
        }, is(10));


        for(Iterable<Measure> measures : client.getCaptured()) {
            // 1x gauge
            // 4x counter (timer,meter,histogram and counter)
            // 2x mean (timer, meter)
            // 2x median (timer, histogram)
            assertThat(Iterables.size(measures), is(9));

            for(Measure measure: measures) {
                assertTrue(measure.getName().startsWith(prefix));
            }
        }

        reporter.close();
        assertThat(client.isClosed(), is(true));
    }

}