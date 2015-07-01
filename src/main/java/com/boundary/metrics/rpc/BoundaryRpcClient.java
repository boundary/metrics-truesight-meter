package com.boundary.metrics.rpc;

import com.boundary.metrics.Measure;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public class BoundaryRpcClient implements BoundaryClient {

    private static final Logger LOG = LoggerFactory.getLogger(BoundaryRpcClient.class);
    private static final String MEASURE_DATA_FORMAT = "_bmetric:%s|v:%f";
    private static final String JSON_RPC_FORMAT = "{\"jsonrpc\":\"2.0\",\"method\":\"metric\",\"params\":{\"data\":\"%s\"}}";

    private final HostAndPort hostAndPort;
    private SocketChannel socketChannel;

    private BoundaryRpcClient(HostAndPort hostAndPort) {

        this.hostAndPort = hostAndPort;
    }

    public static BoundaryRpcClient newInstance(HostAndPort hostAndPort) {
        return new BoundaryRpcClient(hostAndPort);
    }

    public synchronized void start() throws IOException {
        final Selector selector = Selector.open();

        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE);
        socketChannel.connect(new InetSocketAddress(hostAndPort.getHostText(), hostAndPort.getPort()));

        socketChannel.finishConnect();

        for (int i = 0; i < 5; i++) {
            int readyChannels = selector.select(1000);
            if (readyChannels == 0) continue;

            for (SelectionKey k : selector.selectedKeys()) {
                if (k.isWritable()) {
                    selector.close();
                    return;
                }
            }
        }

        throw new IOException("Unable to get ready state connecting to " + hostAndPort.toString());

    }

    @Override
    public void addMeasures(Iterable<Measure> measures) {
        LOG.debug(measures.toString());
        // todo consolidate to a single list first
        for (Measure measure : measures) {
            ByteBuffer buffer = toByteBuff(measure);

            // todo: this doesn't deal with reconnects at all
            // will look into netty instead of straight socket
            try {
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }

        }
    }

    private ByteBuffer toByteBuff(Measure measure) {
        return ByteBuffer.wrap(String.format(JSON_RPC_FORMAT, toMeasureString(measure)).getBytes(Charsets.UTF_8));
    }

    private String toMeasureString(Measure measure) {
        return String.format(MEASURE_DATA_FORMAT, measure.getName(), measure.getValue());
    }

    @Override
    public void close() throws IOException {

        if (socketChannel != null) {
            socketChannel.close();
        }

    }
}
