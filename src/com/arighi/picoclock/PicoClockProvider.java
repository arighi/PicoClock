package com.arighi.picoclock.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;

public class PicoClockProvider extends AppWidgetProvider {

    private static final String LOG_TAG = "PicoClock";

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");

    public static String CLOCK_WIDGET_UPDATE = "com.arighi.picoclock.widget.CLOCK_WIDGET_UPDATE";

    private static PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    protected static void timerOn(Context context) {
        Log.d(LOG_TAG, "enable timer");

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000, createClockTickIntent(context));
    }

    protected static void timerOff(Context context) {
        Log.d(LOG_TAG, "disable timer");

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));
    }

    public void onDisabled(Context context) {
        super.onDisabled(context);
        timerOff(context);
        Log.d(LOG_TAG, "stop service");
        context.stopService(new Intent(context, PicoClockService.class));
    }

    public void onEnabled(Context context) {
        super.onEnabled(context);
        timerOn(context);
        Log.d(LOG_TAG, "start service");
        context.startService(new Intent(context, PicoClockService.class));
    }

    private void refreshWidget(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
            Log.d(LOG_TAG, "update clock");

            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            refreshWidget(context, appWidgetManager, appWidgetIds);
        }
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(LOG_TAG, "update events");
        refreshWidget(context, appWidgetManager, appWidgetIds);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String currentTime = df.format(new Date());

        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        updateViews.setTextViewText(R.id.widget_label, currentTime);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }
}
