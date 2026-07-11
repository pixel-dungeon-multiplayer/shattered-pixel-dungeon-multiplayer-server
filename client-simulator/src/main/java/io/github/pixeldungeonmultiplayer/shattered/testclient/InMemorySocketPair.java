package io.github.pixeldungeonmultiplayer.shattered.testclient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class InMemorySocketPair {

    private final Endpoint client;
    private final Endpoint server;

    @Contract(pure = true)
    private InMemorySocketPair(Endpoint client, Endpoint server) {
        this.client = client;
        this.server = server;
    }

    public static @NotNull InMemorySocketPair create() throws IOException {
        StreamPair clientToServer = StreamPair.create();
        StreamPair serverToClient = StreamPair.create();

        Endpoint client = new Endpoint(serverToClient.input(), clientToServer.output());
        Endpoint server = new Endpoint(clientToServer.input(), serverToClient.output());
        return new InMemorySocketPair(client, server);
    }

    @Contract(pure = true)
    public Endpoint client() {
        return client;
    }

    @Contract(pure = true)
    public Endpoint server() {
        return server;
    }

    private static final class StreamPair {
        private final java.io.PipedInputStream input;
        private final java.io.PipedOutputStream output;

        @Contract(pure = true)
        private StreamPair(java.io.PipedInputStream input, java.io.PipedOutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Contract(pure = true)
        static @NotNull StreamPair create() throws IOException {
            java.io.PipedInputStream input = new java.io.PipedInputStream(64 * 1024);
            java.io.PipedOutputStream output = new java.io.PipedOutputStream(input);
            return new StreamPair(input, output);
        }

        @Contract(pure = true)
        InputStream input() {
            return input;
        }

        @Contract(pure = true)
        OutputStream output() {
            return output;
        }
    }

    public static final class Endpoint extends Socket {
        private final @NotNull InputStream input;
        private final @NotNull OutputStream output;
        private volatile boolean closed;
        private int timeout;

        private Endpoint(@NotNull InputStream input, @NotNull OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Contract(pure = true)
        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Contract(pure = true)
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
            this.timeout = timeout;
        }
    }
}
