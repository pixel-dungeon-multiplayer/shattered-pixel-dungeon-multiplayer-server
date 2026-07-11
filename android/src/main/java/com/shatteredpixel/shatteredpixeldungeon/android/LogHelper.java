package com.shatteredpixel.shatteredpixeldungeon.android;

import android.util.Log;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class LogHelper {
    private static final int MAX_LOGS = 200;
    private static final List<String> logs = new ArrayList<>();
    private static LogListener listener;
    private static boolean initialized = false;

    public interface LogListener {
        void onLogAdded(String line);
        void onLogsCleared();
    }

    public static synchronized void setListener(LogListener newListener) {
        listener = newListener;
        if (listener != null) {
            for (String log : logs) {
                listener.onLogAdded(log);
            }
        }
    }

    public static synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public static synchronized void addLog(String line) {
        logs.add(line);
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
        if (listener != null) {
            try {
                listener.onLogAdded(line);
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void clearLogs() {
        logs.clear();
        if (listener != null) {
            try {
                listener.onLogsCleared();
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        PrintStream origOut = System.out;
        PrintStream origErr = System.err;

        System.setOut(new PrintStream(new OutputStream() {
            private final StringBuilder lineBuilder = new StringBuilder();

            @Override
            public void write(int b) {
                origOut.write(b);
                if (b == '\n') {
                    String line = lineBuilder.toString();
                    Log.d("SPDMP-Server", line);
                    addLog(line);
                    lineBuilder.setLength(0);
                } else if (b != '\r') {
                    lineBuilder.append((char) b);
                }
            }
        }));

        System.setErr(new PrintStream(new OutputStream() {
            private final StringBuilder lineBuilder = new StringBuilder();

            @Override
            public void write(int b) {
                origErr.write(b);
                if (b == '\n') {
                    String line = lineBuilder.toString();
                    Log.e("SPDMP-Server", line);
                    addLog("[ERR] " + line);
                    lineBuilder.setLength(0);
                } else if (b != '\r') {
                    lineBuilder.append((char) b);
                }
            }
        }));
    }
}
