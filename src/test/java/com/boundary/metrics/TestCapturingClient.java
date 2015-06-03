package com.boundary.metrics;

import com.boundary.metrics.rpc.BoundaryClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class TestCapturingClient implements BoundaryClient {
    CopyOnWriteArrayList<Iterable<Measure>> captured = new CopyOnWriteArrayList<>();
    private AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void addMeasures(Iterable<Measure> metrics) {
        captured.add(metrics);
    }

    public List<Iterable<Measure>> getCaptured () {
        return captured;
    }

    boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws IOException {
        closed.compareAndSet(false, true);
    }
}
