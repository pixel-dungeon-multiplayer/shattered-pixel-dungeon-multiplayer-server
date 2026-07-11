package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shatteredpixel.shatteredpixeldungeon.android.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogActivity extends Activity {

    private TextView logTextView;
    private ScrollView logScrollView;

    // Цвета темы
    private int themeBg;
    private int textColor;
    private int textColorSec;
    private int cardBgColor;
    private int cardStrokeColor;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<String> pendingLogs = new ArrayList<>();

    private final Runnable updateLogsRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (pendingLogs) {
                if (pendingLogs.isEmpty()) return;
                StringBuilder batch = new StringBuilder();
                for (String line : pendingLogs) {
                    batch.append(line).append("\n");
                }
                pendingLogs.clear();
                logTextView.append(batch.toString());
            }
            logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
        }
    };

    private final BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.shatteredpixel.shatteredpixeldungeon.android.LOG".equals(action)) {
                String line = intent.getStringExtra("line");
                if (line != null) {
                    synchronized (pendingLogs) {
                        pendingLogs.add(line);
                    }
                    handler.removeCallbacks(updateLogsRunnable);
                    handler.postDelayed(updateLogsRunnable, 100);
                }
            } else if ("com.shatteredpixel.shatteredpixeldungeon.android.CLEAR_LOGS".equals(action)) {
                synchronized (pendingLogs) {
                    pendingLogs.clear();
                }
                logTextView.setText("");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initThemeColors();

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(themeBg);
        rootLayout.setPadding(24, 24, 24, 24);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Кастомный тулбар (Назад, Заголовок, Очистить)
        LinearLayout toolbarLayout = new LinearLayout(this);
        toolbarLayout.setOrientation(LinearLayout.HORIZONTAL);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setPadding(0, 0, 0, 16);

        Button backButton = new Button(this);
        backButton.setText(getString(R.string.btn_back));
        backButton.setTextSize(14);
        backButton.setTextColor(textColor);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(cardBgColor);
        backBg.setCornerRadius(8f);
        backBg.setStroke(1, cardStrokeColor);
        backButton.setBackground(backBg);
        backButton.setPadding(24, 12, 24, 12);
        backButton.setOnClickListener(v -> finish());
        toolbarLayout.addView(backButton);

        TextView titleTextView = new TextView(this);
        titleTextView.setText(getString(R.string.log_screen_title));
        titleTextView.setTextColor(textColor);
        titleTextView.setTextSize(20);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        titleTextView.setLayoutParams(titleParams);
        toolbarLayout.addView(titleTextView);

        Button clearButton = new Button(this);
        clearButton.setText(getString(R.string.btn_clear_log));
        clearButton.setTextSize(14);
        clearButton.setTextColor(textColor);
        GradientDrawable clearBg = new GradientDrawable();
        clearBg.setColor(cardBgColor);
        clearBg.setCornerRadius(8f);
        clearBg.setStroke(1, cardStrokeColor);
        clearButton.setBackground(clearBg);
        clearButton.setPadding(24, 12, 24, 12);
        clearButton.setOnClickListener(v -> LogHelper.clearLogs());
        toolbarLayout.addView(clearButton);

        rootLayout.addView(toolbarLayout);

        // Панель логов
        logScrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        logScrollView.setLayoutParams(scrollParams);

        GradientDrawable logBg = new GradientDrawable();
        logBg.setColor(Color.parseColor("#0A0A0A"));
        logBg.setCornerRadius(12f);
        logBg.setStroke(2, Color.parseColor("#222222"));
        logScrollView.setBackground(logBg);
        logScrollView.setPadding(24, 24, 24, 24);

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

        // Регистрируем бродкаст ресивер для логов
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.shatteredpixel.shatteredpixeldungeon.android.LOG");
        filter.addAction("com.shatteredpixel.shatteredpixeldungeon.android.CLEAR_LOGS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(logReceiver, filter);
        }

        // Асинхронно загружаем историю логов из файлов
        loadLogHistoryAsync();
    }

    private void loadLogHistoryAsync() {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            
            // Сначала считываем бэкап, если он есть
            File backupFile = new File(getFilesDir(), "server_logs.txt.bak");
            if (backupFile.exists()) {
                readLogFile(backupFile, sb);
            }
            
            // Затем считываем текущий файл
            File logFile = new File(getFilesDir(), "server_logs.txt");
            if (logFile.exists()) {
                readLogFile(logFile, sb);
            }
            
            final String historyText = sb.toString();
            runOnUiThread(() -> {
                logTextView.setText(historyText);
                logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
            });
        }, "LogHistoryReader").start();
    }

    private void readLogFile(File file, StringBuilder sb) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ignored) {}
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(logReceiver);
        } catch (Exception ignored) {}
        handler.removeCallbacks(updateLogsRunnable);
        super.onDestroy();
    }

    private void initThemeColors() {
        themeBg = getThemeColor(android.R.attr.colorBackground, Color.parseColor("#F5F5F5"));
        textColor = getThemeColor(android.R.attr.textColorPrimary, Color.BLACK);
        textColorSec = getThemeColor(android.R.attr.textColorSecondary, Color.DKGRAY);

        boolean isDark = isColorDark(themeBg);
        cardBgColor = isDark ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFFFFF");
        cardStrokeColor = isDark ? Color.parseColor("#333333") : Color.parseColor("#E0E0E0");
    }

    private int getThemeColor(int attr, int defaultColor) {
        int[] attrs = new int[] { attr };
        android.content.res.TypedArray ta = obtainStyledAttributes(attrs);
        int color = ta.getColor(0, defaultColor);
        ta.recycle();
        return color;
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
