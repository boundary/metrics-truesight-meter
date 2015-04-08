package com.boundary.metrics;

import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public interface MetricExtension {

    public static ImmutableSet<MetricExtension> ALL = Joiner.all();


    public static class Joiner {
        public static String join(String prefix, String k, MetricExtension ext) {
            return prefix + "." + k + "." + ext.getName();
        }

        public static ImmutableSet<MetricExtension> all() {
            ImmutableSet.Builder<MetricExtension> b = ImmutableSet.builder();
            b.add(MeteredExtension.values());
            b.add(SamplingExtension.values());
            b.add(CountingExtension.values());
            return b.build();

        }
    }


    String getName();



    /**
     * filters corresponding to an {@link com.codahale.metrics.Snapshot}
     */
    enum SamplingExtension implements MetricExtension {

        /* I wish this was java8 */
        MEAN("mean", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.getMean();
            }
        }),
        MEDIAN("median", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.getMedian();
            }
        }),
        P75("75th", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.get75thPercentile();
            }
        }),
        P95("95th", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.get95thPercentile();
            }
        }),
        P98("98th", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.get98thPercentile();
            }
        }),
        P99("99th", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.get99thPercentile();
            }
        }),
        P999("999th", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.get999thPercentile();
            }
        }),
        STDDEV("stddev", new Function<Snapshot, Double>() {
            @Override
            public Double apply(Snapshot input) {
                return input.getStdDev();
            }
        });

        private final String name;
        private final Function<Snapshot, Double> getValue;

        SamplingExtension(String name, Function<Snapshot, Double> getValue) {
            this.name = name;
            this.getValue = getValue;
        }

        @Override
        public String getName() {
            return name;
        }

        public Double getValue(Snapshot snapshot) {
            return getValue.apply(snapshot);
        }
    }

    enum MeteredExtension implements MetricExtension {

        MEANRATE("mean", new Function<Metered, Double>()  {
            @Override
            public Double apply(Metered metered) {
                return metered.getMeanRate();
            }
        }),
        ONEMRATE("1MinuteRate", new Function<Metered, Double>() {
            @Override
            public Double apply(Metered metered) {
                return metered.getOneMinuteRate();
            }
        }),
        FIVEMRATE("5MinuteRate", new Function<Metered, Double>() {
            @Override
            public Double apply(Metered metered) {
                return metered.getFiveMinuteRate();
            }
        }),
        FIFTEENMRATE("15MinuteRate", new Function<Metered, Double>() {
            @Override
            public Double apply(Metered metered) {
                return metered.getFifteenMinuteRate();
            }
        });

        private final String name;
        private final Function<Metered, Double> getValue;

        MeteredExtension(String name, Function<Metered, Double> getValue) {
            this.name = name;
            this.getValue = getValue;
        }

        @Override
        public String getName() {
            return name;
        }

        public Double getValue(Metered metered) {
            return getValue.apply(metered);
        }
    }

    enum CountingExtension implements MetricExtension {
        COUNT("count");

        private final String name;

        CountingExtension(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}


