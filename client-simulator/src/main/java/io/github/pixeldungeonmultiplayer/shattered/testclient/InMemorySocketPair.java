package io.github.pixeldungeonmultiplayer.shattered.testclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class InMemorySocketPair {

    private final Endpoint client;
    private final Endpoint server;

    private InMemorySocketPair(Endpoint client, Endpoint server) {
        this.client = client;
        this.server = server;
    }

    public static InMemorySocketPair create() throws IOException {
        StreamPair clientToServer = StreamPair.create();
        StreamPair serverToClient = StreamPair.create();

        Endpoint client = new Endpoint(serverToClient.input(), clientToServer.output());
        Endpoint server = new Endpoint(clientToServer.input(), serverToClient.output());
        return new InMemorySocketPair(client, server);
    }

    public Endpoint client() {
        return client;
    }

    public Endpoint server() {
        return server;
    }

    private static final class StreamPair {
        private final java.io.PipedInputStream input;
        private final java.io.PipedOutputStream output;

        private StreamPair(java.io.PipedInputStream input, java.io.PipedOutputStream output) {
            this.input = input;
            this.output = output;
        }

        static StreamPair create() throws IOException {
            java.io.PipedInputStream input = new java.io.PipedInputStream(64 * 1024);
            java.io.PipedOutputStream output = new java.io.PipedOutputStream(input);
            return new StreamPair(input, output);
        }

        InputStream input() {
            return input;
        }

        OutputStream output() {
            return output;
        }
    }

    public static final class Endpoint extends Socket {
        private final InputStream input;
        private final OutputStream output;
        private volatile boolean closed;

        private Endpoint(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            IOException first = null;
            try {
                input.close();
            } catch (IOException e) {
                first = e;
            }
            try {
                output.close();
            } catch (IOException e) {
                if (first == null) {
                    first = e;
                }
            }
            if (first != null) {
                throw first;
            }
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void setSoTimeout(int timeout) {
        }
    }
}
