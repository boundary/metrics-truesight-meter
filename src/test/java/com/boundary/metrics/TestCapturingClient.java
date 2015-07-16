package com.boundary.metrics;

import com.boundary.meter.client.BoundaryMeterClient;
import com.boundary.meter.client.command.GetProcessInfo;
import com.boundary.meter.client.command.GetProcessTopK;
import com.boundary.meter.client.model.Event;
import com.boundary.meter.client.model.Measure;
import com.boundary.meter.client.response.DebugResponse;
import com.boundary.meter.client.response.DiscoveryResponse;
import com.boundary.meter.client.response.GetProcessInfoResponse;
import com.boundary.meter.client.response.GetProcessTopKResponse;
import com.boundary.meter.client.response.GetServiceListenersResponse;
import com.boundary.meter.client.response.GetSystemInfoResponse;
import com.boundary.meter.client.response.ImmutableVoidResponse;
import com.boundary.meter.client.response.QueryMetricResponse;
import com.boundary.meter.client.response.VoidResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<VoidResponse> addMeasures(List<Measure> measures) {
        captured.add(measures);
        return CompletableFuture.completedFuture(ImmutableVoidResponse.of());
    }

    @Override
    public CompletableFuture<VoidResponse> addMeasure(Measure measure) {
        return null;
    }

    @Override
    public CompletableFuture<VoidResponse> addEvents(List<Event> events) {
        return null;
    }

    @Override
    public CompletableFuture<VoidResponse> addEvent(Event event) {
        return null;
    }

    @Override
    public CompletableFuture<DiscoveryResponse> discovery() {
        return null;
    }

    @Override
    public CompletableFuture<DebugResponse> debug(String section, int level) {
        return null;
    }

    @Override
    public CompletableFuture<GetSystemInfoResponse> systemInformation() {
        return null;
    }

    @Override
    public CompletableFuture<QueryMetricResponse> queryMetric(String metric, boolean Exact) {
        return null;
    }

    @Override
    public CompletableFuture<GetProcessInfoResponse> getProcessInfo(GetProcessInfo.TypedExpression expression, GetProcessInfo.TypedExpression... optional) {
        return null;
    }

    @Override
    public CompletableFuture<GetProcessTopKResponse> getProcessTopK(GetProcessTopK.TypedNumber number, GetProcessTopK.TypedNumber... optional) {
        return null;
    }

    @Override
    public CompletableFuture<GetServiceListenersResponse> getServiceListeners() {
        return null;
    }

    public List<Iterable<Measure>> getCaptured () {
        return captured;
    }

}
