package com.example.fitlifetracker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.fitlifetracker.R;
import com.example.fitlifetracker.services.TrackingService;

public class FitLifeWidgetProvider extends AppWidgetProvider {

    private static boolean isTracking = false;
    public static final String ACTION_TOGGLE_TRACKING = "com.example.fitlifetracker.ACTION_TOGGLE_TRACKING";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fitlife_widget);

        Intent intent = new Intent(context, FitLifeWidgetProvider.class);
        intent.setAction(ACTION_TOGGLE_TRACKING);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent);
        views.setImageViewResource(R.id.widget_toggle_button, isTracking ? R.drawable.ic_pause : R.drawable.ic_play);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TOGGLE_TRACKING.equals(intent.getAction())) {
            isTracking = !isTracking;
            Intent serviceIntent = new Intent(context, TrackingService.class);
            if (isTracking) {
                context.startForegroundService(serviceIntent);
            } else {
                context.stopService(serviceIntent);
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, FitLifeWidgetProvider.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }
}