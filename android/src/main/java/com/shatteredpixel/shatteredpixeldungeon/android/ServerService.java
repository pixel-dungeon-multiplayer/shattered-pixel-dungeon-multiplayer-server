package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import com.shatteredpixel.shatteredpixeldungeon.android.R;

public class ServerService extends Service {

    public static final String ACTION_STOP_SERVER = "com.shatteredpixel.shatteredpixeldungeon.android.ACTION_STOP_SERVER";
    private static final String CHANNEL_ID = "SPDMP_Server_Channel";
    private static final int NOTIFICATION_ID = 42;

    private static boolean running = false;

    private final BroadcastReceiver statusRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.shatteredpixel.shatteredpixeldungeon.android.REQUEST_STATUS".equals(intent.getAction())) {
                broadcastStatus(running);
            }
        }
    };

    private void broadcastStatus(boolean isRunning) {
        Intent intent = new Intent("com.shatteredpixel.shatteredpixeldungeon.android.STATUS");
        intent.putExtra("running", isRunning);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        IntentFilter filter = new IntentFilter("com.shatteredpixel.shatteredpixeldungeon.android.REQUEST_STATUS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusRequestReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(statusRequestReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_SERVER.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Если служба уже работает, игнорируем повторный вызов
        if (running) {
            return START_NOT_STICKY;
        }

        running = true;
        LogHelper.init(this); // Инициализируем логгер с контекстом сервиса

        Notification notification = createNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int foregroundServiceType = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // FOREGROUND_SERVICE_TYPE_SPECIAL_USE is 0x40000000 (1073741824)
                foregroundServiceType = 1073741824;
            }
            if (foregroundServiceType != 0) {
                startForeground(NOTIFICATION_ID, notification, foregroundServiceType);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        broadcastStatus(true);

        new Thread(() -> {
            AndroidHeadlessServerLauncher.launch(ServerService.this);
        }, "HeadlessServerThread").start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        AndroidHeadlessServerLauncher.stop();
        running = false;
        broadcastStatus(false);
        try {
            unregisterReceiver(statusRequestReceiver);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // В многопроцессном режиме локальная привязка не поддерживается
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Shattered PD Server Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, ServerActivity.class);
        
        int pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                pendingIntentFlags
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        int iconRes = android.R.drawable.ic_media_play;
        try {
            int resId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
            if (resId != 0) iconRes = resId;
        } catch (Exception ignored) {}

        Intent stopIntent = new Intent(this, ServerService.class);
        stopIntent.setAction(ACTION_STOP_SERVER);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return builder
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(iconRes)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.btn_stop), stopPendingIntent)
                .build();
    }
}
