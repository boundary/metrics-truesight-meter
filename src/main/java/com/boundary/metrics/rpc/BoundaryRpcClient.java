package com.boundary.metrics.rpc;

import com.boundary.metrics.Measure;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class BoundaryRpcClient implements BoundaryClient {
    public static BoundaryRpcClient newInstance() {
        return new BoundaryRpcClient();
    }

    @Override
    public void addMeasures(Iterable<Measure> metrics) {
        throw new NotImplementedException();
    }
}
