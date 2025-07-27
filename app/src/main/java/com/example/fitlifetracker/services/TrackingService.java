package com.example.fitlifetracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.fitlifetracker.R;
import com.example.fitlifetracker.widget.FitLifeWidgetProvider;

public class TrackingService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "fitlife_channel";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int steps = 0;
    private double calories = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
        handler.post(stepSimulator);
    }

    private final Runnable stepSimulator = new Runnable() {
        @Override
        public void run() {
            steps += (int) (Math.random() * 3) + 1;
            calories = steps * 0.04;
            updateWidget();
            handler.postDelayed(this, 2000);
        }
    };

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FitLifeWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.fitlife_widget);
            views.setTextViewText(R.id.widget_steps_value, String.valueOf(steps));
            views.setTextViewText(R.id.widget_calories_value, String.format("%.0f", calories));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(stepSimulator);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("FitLife Tracker")
                .setContentText("Activity tracking is active.")
                .setSmallIcon(R.drawable.ic_calories)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "FitLife Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}