package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends Activity {

    private TextView statusValue;
    private TextView ipAddressValue;
    private Button startButton;
    private Button stopButton;
    private TextView logTextView;
    private ScrollView logScrollView;

    private ServerService serverService;
    private boolean isBound = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusUpdater = new Runnable() {
        @Override
        public void run() {
            updateStatusUI();
            handler.postDelayed(this, 1000);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ServerService.LocalBinder binder = (ServerService.LocalBinder) service;
            serverService = binder.getService();
            isBound = true;
            updateStatusUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
            serverService = null;
            updateStatusUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.init();

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#121212"));
        rootLayout.setPadding(32, 32, 32, 32);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView titleTextView = new TextView(this);
        titleTextView.setText("Shattered PD Server");
        titleTextView.setTextColor(Color.parseColor("#FFFFFF"));
        titleTextView.setTextSize(24);
        titleTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setPadding(0, 0, 0, 24);
        rootLayout.addView(titleTextView);

        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(24, 24, 24, 24);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1E1E1E"));
        cardBg.setCornerRadius(16f);
        cardBg.setStroke(2, Color.parseColor("#333333"));
        cardLayout.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        cardLayout.setLayoutParams(cardParams);

        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setPadding(0, 0, 0, 16);

        TextView statusLabel = new TextView(this);
        statusLabel.setText("Статус: ");
        statusLabel.setTextColor(Color.parseColor("#BBBBBB"));
        statusLabel.setTextSize(16);
        statusRow.addView(statusLabel);

        statusValue = new TextView(this);
        statusValue.setText("Остановлен");
        statusValue.setTextColor(Color.parseColor("#FF5555"));
        statusValue.setTextSize(16);
        statusValue.setTypeface(Typeface.DEFAULT_BOLD);
        statusRow.addView(statusValue);

        cardLayout.addView(statusRow);

        LinearLayout ipRow = new LinearLayout(this);
        ipRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView ipLabel = new TextView(this);
        ipLabel.setText("IP в сети: ");
        ipLabel.setTextColor(Color.parseColor("#BBBBBB"));
        ipLabel.setTextSize(16);
        ipRow.addView(ipLabel);

        ipAddressValue = new TextView(this);
        ipAddressValue.setText(getLocalIpAddress());
        ipAddressValue.setTextColor(Color.parseColor("#55FF55"));
        ipAddressValue.setTextSize(16);
        ipAddressValue.setTypeface(Typeface.DEFAULT_BOLD);
        ipAddressValue.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Server IP", ipAddressValue.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ServerActivity.this, "IP скопирован в буфер", Toast.LENGTH_SHORT).show();
            }
        });
        ipRow.addView(ipAddressValue);

        cardLayout.addView(ipRow);
        rootLayout.addView(cardLayout);

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(0, 0, 0, 24);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        startButton = new Button(this);
        startButton.setText("Запустить");
        startButton.setTextSize(16);
        startButton.setTextColor(Color.WHITE);
        GradientDrawable startBtnBg = new GradientDrawable();
        startBtnBg.setColor(Color.parseColor("#1B5E20"));
        startBtnBg.setCornerRadius(8f);
        startButton.setBackground(startBtnBg);
        btnParams.setMargins(0, 0, 8, 0);
        startButton.setLayoutParams(btnParams);
        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(ServerActivity.this, ServerService.class);
            startService(serviceIntent);
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            Toast.makeText(ServerActivity.this, "Запуск сервера...", Toast.LENGTH_SHORT).show();
        });
        buttonsLayout.addView(startButton);

        stopButton = new Button(this);
        stopButton.setText("Остановить");
        stopButton.setTextSize(16);
        stopButton.setTextColor(Color.WHITE);
        GradientDrawable stopBtnBg = new GradientDrawable();
        stopBtnBg.setColor(Color.parseColor("#B71C1C"));
        stopBtnBg.setCornerRadius(8f);
        stopButton.setBackground(stopBtnBg);
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        stopParams.setMargins(8, 0, 0, 0);
        stopButton.setLayoutParams(stopParams);
        stopButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(ServerActivity.this, ServerService.class);
            stopService(serviceIntent);
            if (isBound) {
                unbindService(connection);
                isBound = false;
            }
            updateStatusUI();
            Toast.makeText(ServerActivity.this, "Сервер остановлен", Toast.LENGTH_SHORT).show();
        });
        buttonsLayout.addView(stopButton);

        rootLayout.addView(buttonsLayout);

        LinearLayout logTitleRow = new LinearLayout(this);
        logTitleRow.setOrientation(LinearLayout.HORIZONTAL);
        logTitleRow.setPadding(0, 0, 0, 8);

        TextView logTitle = new TextView(this);
        logTitle.setText("Лог сервера:");
        logTitle.setTextColor(Color.parseColor("#888888"));
        logTitle.setTextSize(14);
        LinearLayout.LayoutParams logTitleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        logTitle.setLayoutParams(logTitleParams);
        logTitleRow.addView(logTitle);

        TextView clearLogBtn = new TextView(this);
        clearLogBtn.setText("Очистить");
        clearLogBtn.setTextColor(Color.parseColor("#00B0FF"));
        clearLogBtn.setTextSize(14);
        clearLogBtn.setPadding(8, 8, 8, 8);
        clearLogBtn.setOnClickListener(v -> LogHelper.clearLogs());
        logTitleRow.addView(clearLogBtn);

        rootLayout.addView(logTitleRow);

        logScrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        logScrollView.setLayoutParams(scrollParams);

        GradientDrawable logBg = new GradientDrawable();
        logBg.setColor(Color.parseColor("#0A0A0A"));
        logBg.setCornerRadius(8f);
        logBg.setStroke(1, Color.parseColor("#222222"));
        logScrollView.setBackground(logBg);
        logScrollView.setPadding(16, 16, 16, 16);

        logTextView = new TextView(this);
        logTextView.setTextColor(Color.parseColor("#00FF00"));
        logTextView.setTextSize(12);
        logTextView.setTypeface(Typeface.MONOSPACE);
        logTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        logScrollView.addView(logTextView);

        rootLayout.addView(logScrollView);

        setContentView(rootLayout);

        LogHelper.setListener(new LogHelper.LogListener() {
            @Override
            public void onLogAdded(String line) {
                runOnUiThread(() -> {
                    logTextView.append(line + "\n");
                    logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
                });
            }

            @Override
            public void onLogsCleared() {
                runOnUiThread(() -> logTextView.setText(""));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ServerService.isRunning()) {
            Intent serviceIntent = new Intent(this, ServerService.class);
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
        handler.post(statusUpdater);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        handler.removeCallbacks(statusUpdater);
    }

    private void updateStatusUI() {
        boolean isRunning = ServerService.isRunning();
        if (isRunning) {
            statusValue.setText("Запущен");
            statusValue.setTextColor(Color.parseColor("#55FF55"));
            startButton.setEnabled(false);
            startButton.setAlpha(0.5f);
            stopButton.setEnabled(true);
            stopButton.setAlpha(1.0f);
        } else {
            statusValue.setText("Остановлен");
            statusValue.setTextColor(Color.parseColor("#FF5555"));
            startButton.setEnabled(true);
            startButton.setAlpha(1.0f);
            stopButton.setEnabled(false);
            stopButton.setAlpha(0.5f);
        }
        ipAddressValue.setText(getLocalIpAddress());
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "Неизвестно";
    }
}
