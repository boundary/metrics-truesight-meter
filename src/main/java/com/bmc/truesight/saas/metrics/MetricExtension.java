package com.bmc.truesight.saas.metrics;

import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;

public interface MetricExtension {

    static ImmutableSet<MetricExtension> all() {
        ImmutableSet.Builder<MetricExtension> b = ImmutableSet.builder();
        b.add(Metering.values());
        b.add(Sampling.values());
        b.add(Counting.values());
        return b.build();

    }

    ImmutableSet<MetricExtension> ALL = all();

    @JsonCreator
    static MetricExtension fromString(String id) {

        for (MetricExtension ext : all()) {
            if (id.equalsIgnoreCase(ext.getName())) {
                return ext;
            }
        }

        throw new IllegalArgumentException("Couldn't create a valid MetricExtension from " + id);

    }


    String getName();


    /**
     * filters corresponding to an {@link com.codahale.metrics.Snapshot}
     */
    enum Sampling implements MetricExtension, Fn.GetValue<Snapshot> {

        Mean("Mean", Snapshot::getMean),
        Median("Median", Snapshot::getMedian),
        P75("75th", Snapshot::get75thPercentile),
        P95("95th",Snapshot::get95thPercentile),
        P98("98th", Snapshot::get98thPercentile),
        P99("99th", Snapshot::get99thPercentile),
        P999("999th", Snapshot::get999thPercentile),
        StdDev("StdDev", Snapshot::getStdDev);

        private final String name;
        private final Fn.GetValue<Snapshot> getValue;

        Sampling(String name, Fn.GetValue<Snapshot> getValue) {
            this.name = name;
            this.getValue = getValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Double getValue(Snapshot snapshot) {
            return getValue.getValue(snapshot);
        }
    }

    enum Metering implements MetricExtension, Fn.GetValue<Metered> {

        MeanRate("MeanRate", Metered::getMeanRate),
        OneMinuteRate("1MinuteRate", Metered::getOneMinuteRate),
        FiveMinuteRate("5MinuteRate", Metered::getFiveMinuteRate),
        FifteenMinuteRate("15MinuteRate", Metered::getFifteenMinuteRate);

        private final String name;
        private final Fn.GetValue<Metered> getValue;

        Metering(String name, Fn.GetValue<Metered> getValue) {
            this.name = name;
            this.getValue = getValue;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Double getValue(Metered metered) {
            return getValue.getValue(metered);
        }
    }

    enum Counting implements MetricExtension {
        COUNT("count");

        private final String name;

        Counting(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}


