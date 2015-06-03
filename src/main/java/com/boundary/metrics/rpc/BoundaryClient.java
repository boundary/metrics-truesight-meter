package com.boundary.metrics.rpc;

import com.boundary.metrics.Measure;

import java.io.Closeable;

public interface BoundaryClient extends Closeable {

    public void addMeasures(Iterable<Measure> metrics);
}
