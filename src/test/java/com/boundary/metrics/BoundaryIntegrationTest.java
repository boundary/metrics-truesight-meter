package com.boundary.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by jesse on 4/6/15.
 */
public class BoundaryIntegrationTest {

    @Test
    public void testMetrics() throws InterruptedException {

        MetricRegistry registry = new MetricRegistry();

        final Counter c = registry.counter(name(getClass().getSimpleName(), "test-counter"));
        final Histogram h = registry.histogram(name(getClass().getSimpleName(), "test-histogram"));
        final Timer t = registry.timer("test-timer");

        final Meter m = registry.meter("test-meter");

        Gauge<Double> inverseCounter = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1.0, c.getCount());
            }
        };
        registry.register("test-gauge", inverseCounter);


        BoundaryReporter reporter = BoundaryReporter.reporter()
                .setDurationUnit(TimeUnit.SECONDS)
                .setRateUnit(TimeUnit.SECONDS)
                .setPrefix("test")
                .setRegistry(registry)
                .build();

        reporter.start(1, TimeUnit.SECONDS);


        for (int i = 0; i < 10; i++) {
            try(Timer.Context ignored = t.time()) {
                c.inc();
                h.update(i);
                m.mark();
            }

            Thread.sleep(1000);
        }




    }

}
