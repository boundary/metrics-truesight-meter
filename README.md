# metrics-boundary

A library for reporting dropwizard metrics to TrueSight Pulse.

# Installation
## Maven

```xml
    <dependency>
        <groupId>com.boundary</groupId>
        <artifactId>metrics-boundary</artifactId>
        <version>0.7</version>
    </dependency>
```

# Usage


```java
        
        MetricRegistry registry = new MetricRegistry();
        // filtering is highly recommended to limit the number of data points reported
        MetricFilter filter = (name, metric) -> name.equals("my-included-metric");
        // extensions are highly recommended to limit the amount data points reported
        Set<MetricExtension> extensions = ImmutableSet.of(MetricExtension.Counting.COUNT, MetricExtension.Metering.OneMinuteRate);

        BoundaryReporter.Builder builder = BoundaryReporter.builder()
                .setDurationUnit(TimeUnit.MILLISECONDS)
                .setFilter(filter)
                .setRateUnit(TimeUnit.SECONDS)
                .setRegistry(registry)
                .setExtensions(extensions)
                .setPrefix("my_app_name");

        BoundaryReporter reporter = builder.build();
        reporter.start(1, TimeUnit.SECONDS);
```

# Tests

To run the tests, clone the repo and run `mvn test` from the parent directory.

# LICENSE

Copyright 2016 BMC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.