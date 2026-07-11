package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shatteredpixel.shatteredpixeldungeon.android.R;

import java.io.File;
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

    // Элементы настроек
    private EditText serverNameInput;
    private EditText serverPortInput;
    private EditText maxPlayersInput;
    private EditText motdInput;
    private CheckBox onlineModeCheckbox;
    private Button saveSettingsButton;
    private Button openLogsButton;
    private Button resetSaveButton;

    private ServerService serverService;
    private boolean isBound = false;

    // Цвета темы
    private int themeBg;
    private int textColor;
    private int textColorSec;
    private int cardBgColor;
    private int cardStrokeColor;
    private int editTextColor;
    private int editTextBgColor;

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
            updateIpAddress();
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
        initThemeColors();

        // Основной скроллер для всей разметки (нужен при появлении клавиатуры)
        ScrollView mainScrollView = new ScrollView(this);
        mainScrollView.setBackgroundColor(themeBg);
        mainScrollView.setFillViewport(true);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 32, 32, 32);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Заголовок
        TextView titleTextView = new TextView(this);
        titleTextView.setText(getString(R.string.app_title));
        titleTextView.setTextColor(textColor);
        titleTextView.setTextSize(24);
        titleTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setPadding(0, 0, 0, 24);
        rootLayout.addView(titleTextView);

        // Карточка статуса и IP
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(24, 24, 24, 24);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(cardBgColor);
        cardBg.setCornerRadius(16f);
        cardBg.setStroke(2, cardStrokeColor);
        cardLayout.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        cardLayout.setLayoutParams(cardParams);

        // Статус
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setPadding(0, 0, 0, 16);

        TextView statusLabel = new TextView(this);
        statusLabel.setText(getString(R.string.status_label));
        statusLabel.setTextColor(textColorSec);
        statusLabel.setTextSize(16);
        statusRow.addView(statusLabel);

        statusValue = new TextView(this);
        statusValue.setText(getString(R.string.status_stopped));
        statusValue.setTextColor(Color.parseColor("#FF5555"));
        statusValue.setTextSize(16);
        statusValue.setTypeface(Typeface.DEFAULT_BOLD);
        statusRow.addView(statusValue);
        cardLayout.addView(statusRow);

        // IP
        LinearLayout ipRow = new LinearLayout(this);
        ipRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView ipLabel = new TextView(this);
        ipLabel.setText(getString(R.string.ip_label));
        ipLabel.setTextColor(textColorSec);
        ipLabel.setTextSize(16);
        ipRow.addView(ipLabel);

        ipAddressValue = new TextView(this);
        ipAddressValue.setText(getString(R.string.ip_unknown));
        ipAddressValue.setTextColor(Color.parseColor("#55FF55"));
        ipAddressValue.setTextSize(16);
        ipAddressValue.setTypeface(Typeface.DEFAULT_BOLD);
        ipAddressValue.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Server IP", ipAddressValue.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ServerActivity.this, getString(R.string.msg_ip_copied), Toast.LENGTH_SHORT).show();
            }
        });
        ipRow.addView(ipAddressValue);

        cardLayout.addView(ipRow);
        rootLayout.addView(cardLayout);

        // Кнопки управления
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(0, 0, 0, 24);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        startButton = new Button(this);
        startButton.setText(getString(R.string.btn_start));
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
            Toast.makeText(ServerActivity.this, getString(R.string.msg_starting_server), Toast.LENGTH_SHORT).show();
            updateIpAddress();
        });
        buttonsLayout.addView(startButton);

        stopButton = new Button(this);
        stopButton.setText(getString(R.string.btn_stop));
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
            Toast.makeText(ServerActivity.this, getString(R.string.msg_server_stopped), Toast.LENGTH_SHORT).show();
            updateIpAddress();
        });
        buttonsLayout.addView(stopButton);
        rootLayout.addView(buttonsLayout);

        // Кнопка открытия логов (отдельный экран)
        openLogsButton = new Button(this);
        openLogsButton.setText(getString(R.string.btn_open_logs));
        openLogsButton.setTextColor(Color.WHITE);
        openLogsButton.setTextSize(16);
        GradientDrawable openLogsBtnBg = new GradientDrawable();
        openLogsBtnBg.setColor(Color.parseColor("#37474F")); // Dark slate blue
        openLogsBtnBg.setCornerRadius(8f);
        openLogsButton.setBackground(openLogsBtnBg);
        LinearLayout.LayoutParams openLogsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        openLogsParams.setMargins(0, 0, 0, 16);
        openLogsButton.setLayoutParams(openLogsParams);
        openLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ServerActivity.this, LogActivity.class);
            startActivity(intent);
        });
        rootLayout.addView(openLogsButton);

        // Кнопка сброса сохранения (опасное действие)
        resetSaveButton = new Button(this);
        resetSaveButton.setText(getString(R.string.btn_reset_save));
        resetSaveButton.setTextColor(Color.WHITE);
        resetSaveButton.setTextSize(16);
        GradientDrawable resetBtnBg = new GradientDrawable();
        resetBtnBg.setColor(Color.parseColor("#D32F2F")); // Red
        resetBtnBg.setCornerRadius(8f);
        resetSaveButton.setBackground(resetBtnBg);
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        resetParams.setMargins(0, 0, 0, 24);
        resetSaveButton.setLayoutParams(resetParams);
        resetSaveButton.setOnClickListener(v -> showResetConfirmationDialog());
        rootLayout.addView(resetSaveButton);

        // Карточка настроек
        LinearLayout settingsCard = new LinearLayout(this);
        settingsCard.setOrientation(LinearLayout.VERTICAL);
        settingsCard.setPadding(24, 24, 24, 24);
        GradientDrawable settingsBg = new GradientDrawable();
        settingsBg.setColor(cardBgColor);
        settingsBg.setCornerRadius(16f);
        settingsBg.setStroke(2, cardStrokeColor);
        settingsCard.setBackground(settingsBg);

        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        settingsCard.setLayoutParams(settingsParams);

        TextView settingsTitle = new TextView(this);
        settingsTitle.setText(getString(R.string.settings_title));
        settingsTitle.setTextColor(textColor);
        settingsTitle.setTextSize(18);
        settingsTitle.setTypeface(Typeface.DEFAULT_BOLD);
        settingsTitle.setPadding(0, 0, 0, 16);
        settingsCard.addView(settingsTitle);

        serverNameInput = new EditText(this);
        settingsCard.addView(createInputField(getString(R.string.settings_name_label), serverNameInput));

        serverPortInput = new EditText(this);
        serverPortInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        settingsCard.addView(createInputField(getString(R.string.settings_port_label), serverPortInput));

        maxPlayersInput = new EditText(this);
        maxPlayersInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        settingsCard.addView(createInputField(getString(R.string.settings_max_players_label), maxPlayersInput));

        motdInput = new EditText(this);
        settingsCard.addView(createInputField(getString(R.string.settings_motd_label), motdInput));

        onlineModeCheckbox = new CheckBox(this);
        onlineModeCheckbox.setText(getString(R.string.settings_online_mode_label));
        onlineModeCheckbox.setTextColor(textColor);
        onlineModeCheckbox.setTextSize(16);
        onlineModeCheckbox.setPadding(0, 8, 0, 16);
        settingsCard.addView(onlineModeCheckbox);

        saveSettingsButton = new Button(this);
        saveSettingsButton.setText(getString(R.string.btn_save_settings));
        saveSettingsButton.setTextColor(Color.WHITE);
        GradientDrawable saveBtnBg = new GradientDrawable();
        saveBtnBg.setColor(Color.parseColor("#0288D1")); // System blue
        saveBtnBg.setCornerRadius(8f);
        saveSettingsButton.setBackground(saveBtnBg);
        saveSettingsButton.setOnClickListener(v -> saveSettings());
        settingsCard.addView(saveSettingsButton);

        rootLayout.addView(settingsCard);

        mainScrollView.addView(rootLayout);
        setContentView(mainScrollView);

        // Инициализируем настройки из Preferences
        loadSettings();
    }

    private void initThemeColors() {
        themeBg = getThemeColor(android.R.attr.colorBackground, Color.parseColor("#F5F5F5"));
        textColor = getThemeColor(android.R.attr.textColorPrimary, Color.BLACK);
        textColorSec = getThemeColor(android.R.attr.textColorSecondary, Color.DKGRAY);

        boolean isDark = isColorDark(themeBg);
        cardBgColor = isDark ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFFFFF");
        cardStrokeColor = isDark ? Color.parseColor("#333333") : Color.parseColor("#E0E0E0");

        editTextColor = textColor;
        editTextBgColor = isDark ? Color.parseColor("#2D2D2D") : Color.parseColor("#ECEFF1");
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

    private LinearLayout createInputField(String labelText, EditText editText) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 16);

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(textColorSec);
        label.setTextSize(14);
        label.setPadding(0, 0, 0, 4);
        row.addView(label);

        editText.setTextColor(editTextColor);
        editText.setTextSize(16);
        editText.setPadding(24, 16, 24, 16);

        GradientDrawable editBg = new GradientDrawable();
        editBg.setColor(editTextBgColor);
        editBg.setCornerRadius(8f);
        editBg.setStroke(1, cardStrokeColor);
        editText.setBackground(editBg);

        row.addView(editText);
        return row;
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("ShatteredPixelDungeonServer", MODE_PRIVATE);
        serverNameInput.setText(prefs.getString("server_name", "SPD-server"));
        serverPortInput.setText(String.valueOf(prefs.getInt("server_port", 0)));
        maxPlayersInput.setText(String.valueOf(prefs.getInt("max_players", 8)));
        motdInput.setText(prefs.getString("motd", ""));
        onlineModeCheckbox.setChecked(prefs.getBoolean("online_mode", true));
    }

    private void saveSettings() {
        String name = serverNameInput.getText().toString().trim();
        String portStr = serverPortInput.getText().toString().trim();
        String maxPlayersStr = maxPlayersInput.getText().toString().trim();
        String motd = motdInput.getText().toString().trim();
        boolean online = onlineModeCheckbox.isChecked();

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_err_empty_name), Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 0 || port > 65535) {
                Toast.makeText(this, getString(R.string.msg_err_invalid_port), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.msg_err_port_format), Toast.LENGTH_SHORT).show();
            return;
        }

        int maxPlayers;
        try {
            maxPlayers = Integer.parseInt(maxPlayersStr);
            if (maxPlayers <= 0) {
                Toast.makeText(this, getString(R.string.msg_err_invalid_players), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.msg_err_players_format), Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("ShatteredPixelDungeonServer", MODE_PRIVATE);
        prefs.edit()
                .putString("server_name", name)
                .putInt("server_port", port)
                .putInt("max_players", maxPlayers)
                .putString("motd", motd)
                .putBoolean("online_mode", online)
                .apply();

        Toast.makeText(this, getString(R.string.msg_settings_saved), Toast.LENGTH_SHORT).show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_reset_title))
                .setMessage(getString(R.string.confirm_reset_msg))
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    deleteSaveData();
                    Toast.makeText(this, getString(R.string.msg_save_deleted), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void deleteSaveData() {
        File externalSaveDir = new File(getExternalFilesDir(null), "save");
        deleteRecursive(externalSaveDir);

        File internalSaveDir = new File(getFilesDir(), "save");
        deleteRecursive(internalSaveDir);
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] children = fileOrDirectory.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ServerService.isRunning()) {
            Intent serviceIntent = new Intent(this, ServerService.class);
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
        handler.post(statusUpdater);
        updateIpAddress();
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
            statusValue.setText(getString(R.string.status_running));
            statusValue.setTextColor(Color.parseColor("#55FF55"));
            startButton.setEnabled(false);
            startButton.setAlpha(0.5f);
            stopButton.setEnabled(true);
            stopButton.setAlpha(1.0f);

            // Блокируем изменение настроек и сброс сохранения во время работы сервера
            serverNameInput.setEnabled(false);
            serverPortInput.setEnabled(false);
            maxPlayersInput.setEnabled(false);
            motdInput.setEnabled(false);
            onlineModeCheckbox.setEnabled(false);
            saveSettingsButton.setEnabled(false);
            saveSettingsButton.setAlpha(0.5f);

            resetSaveButton.setEnabled(false);
            resetSaveButton.setAlpha(0.5f);
        } else {
            statusValue.setText(getString(R.string.status_stopped));
            statusValue.setTextColor(Color.parseColor("#FF5555"));
            startButton.setEnabled(true);
            startButton.setAlpha(1.0f);
            stopButton.setEnabled(false);
            stopButton.setAlpha(0.5f);

            // Разрешаем изменение настроек и сброс сохранения, когда сервер остановлен
            serverNameInput.setEnabled(true);
            serverPortInput.setEnabled(true);
            maxPlayersInput.setEnabled(true);
            motdInput.setEnabled(true);
            onlineModeCheckbox.setEnabled(true);
            saveSettingsButton.setEnabled(true);
            saveSettingsButton.setAlpha(1.0f);

            resetSaveButton.setEnabled(true);
            resetSaveButton.setAlpha(1.0f);
        }
    }

    // Асинхронное получение IP в фоновом потоке, чтобы исключить блокировки UI
    private void updateIpAddress() {
        new Thread(() -> {
            final String ip = getLocalIpAddress();
            runOnUiThread(() -> {
                if (ipAddressValue != null) {
                    ipAddressValue.setText(ip);
                }
            });
        }, "IpQueryThread").start();
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
        return getString(R.string.ip_unknown);
    }
}
