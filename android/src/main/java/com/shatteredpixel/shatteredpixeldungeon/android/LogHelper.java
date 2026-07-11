package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class LogHelper {
    private static Context appContext;
    private static File logFile;
    private static boolean initialized = false;

    public interface LogListener {
        void onLogAdded(String line);
        void onLogsCleared();
    }

    // Заглушка для обратной совместимости
    public static synchronized void setListener(LogListener newListener) {
    }

    // Заглушка для обратной совместимости
    public static synchronized List<String> getLogs() {
        return new ArrayList<>();
    }

    private static void checkLogRotation() {
        if (logFile != null && logFile.exists() && logFile.length() > 100 * 1024) { // 100 KB limit
            try {
                File backupFile = new File(appContext.getFilesDir(), "server_logs.txt.bak");
                backupFile.delete();
                logFile.renameTo(backupFile);
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void addLog(String line) {
        if (appContext != null) {
            checkLogRotation();
        }

        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(line);
                bw.newLine();
            } catch (IOException ignored) {}
        }

        if (appContext != null) {
            Intent intent = new Intent("com.shatteredpixel.shatteredpixeldungeon.android.LOG");
            intent.putExtra("line", line);
            appContext.sendBroadcast(intent);
        }
    }

    public static synchronized void clearLogs() {
        if (logFile != null) {
            logFile.delete();
        }
        File backupFile = new File(appContext.getFilesDir(), "server_logs.txt.bak");
        backupFile.delete();

        if (appContext != null) {
            Intent intent = new Intent("com.shatteredpixel.shatteredpixeldungeon.android.CLEAR_LOGS");
            appContext.sendBroadcast(intent);
        }
    }

    public static synchronized void init(Context context) {
        appContext = context.getApplicationContext();
        logFile = new File(appContext.getFilesDir(), "server_logs.txt");
        logFile.delete(); // Стираем старый лог при каждом старте сервера
        File backupFile = new File(appContext.getFilesDir(), "server_logs.txt.bak");
        backupFile.delete(); // Стираем старый бэкап при каждом старте сервера

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
