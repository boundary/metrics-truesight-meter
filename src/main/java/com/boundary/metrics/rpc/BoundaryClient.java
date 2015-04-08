package com.boundary.metrics.rpc;

import com.boundary.metrics.Measure;

public interface BoundaryClient {

    public void addMeasures(Iterable<Measure> metrics);
}
