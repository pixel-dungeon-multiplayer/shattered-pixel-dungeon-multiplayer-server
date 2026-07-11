package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class ServerService extends Service {

    private static final String CHANNEL_ID = "SPDMP_Server_Channel";
    private static final int NOTIFICATION_ID = 42;

    private final IBinder binder = new LocalBinder();
    private static boolean running = false;

    public class LocalBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

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

        new Thread(() -> {
            AndroidHeadlessServerLauncher.launch(ServerService.this);
        }, "HeadlessServerThread").start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        AndroidHeadlessServerLauncher.stop();
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static boolean isRunning() {
        return running;
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

        return builder
                .setContentTitle("Shattered PD Multiplayer Server")
                .setContentText("Сервер запущен и работает в фоне")
                .setSmallIcon(iconRes)
                .setContentIntent(pendingIntent)
                .build();
    }
}
