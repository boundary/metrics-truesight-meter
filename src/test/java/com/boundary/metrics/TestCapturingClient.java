package com.boundary.metrics;

import com.boundary.meter.client.BoundaryMeterClient;
import com.boundary.meter.client.command.DiscoveryResponse;
import com.boundary.meter.client.command.GetServiceListenersResponse;
import com.boundary.meter.client.command.VoidResponse;
import com.boundary.meter.client.model.Measure;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class TestCapturingClient implements BoundaryMeterClient {
    CopyOnWriteArrayList<Iterable<Measure>> captured = new CopyOnWriteArrayList<>();
    private AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void close() throws IOException {
        closed.compareAndSet(false, true);
    }
    boolean isClosed() {
        return closed.get();
    }

    @Override
    public ListenableFuture<VoidResponse> addMeasures(List<Measure> measures) {
        captured.add(measures);
        return Futures.immediateFuture(new VoidResponse(-1));
    }

    public List<Iterable<Measure>> getCaptured () {
        return captured;
    }
    @Override
    public ListenableFuture<DiscoveryResponse> discovery() {
        return null;
    }

    @Override
    public ListenableFuture<GetServiceListenersResponse> getServiceListeners() {
        return null;
    }
}
