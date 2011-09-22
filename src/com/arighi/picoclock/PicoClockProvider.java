package com.arighi.picoclock.widget;

import java.lang.Runtime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

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

    private static final DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

    private static CpuUsage cpuUsage = null;
    private static MemoryUsage memUsage = null;

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
        try {
            cpuUsage.close();
            memUsage.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "failed to close the cpuUsage or memUsage object: " + e);
        }
        timerOff(context);
        Log.d(LOG_TAG, "stop service");
        context.stopService(new Intent(context, PicoClockService.class));
    }

    public void onEnabled(Context context) {
        super.onEnabled(context);
        cpuUsage = new CpuUsage();
        memUsage = new MemoryUsage();
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

    private static String readFile(String filename) {
        File file = new File(filename);
        StringBuilder text = new StringBuilder();
        BufferedReader buf = null;

        try {
            buf = new BufferedReader(new FileReader(file));
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line);
            }
            buf.close();
        } catch (java.io.IOException e) {
            Log.e(LOG_TAG, "failed to read " + filename);
        }

        return text.toString();
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Runtime runtime = Runtime.getRuntime();

	try {
	        cpuUsage.update();
	} catch (Exception e) {
		Log.e(LOG_TAG, "failed to update cpu statistics: " + e);
	}
	try {
	        memUsage.update();
	} catch (Exception e) {
		Log.e(LOG_TAG, "failed to update memory statistics: " + e);
	}

        int cpu_freq;
        try {
            cpu_freq = Integer.parseInt(readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")) / 1000;
        } catch (Exception e) {
            cpu_freq = 0;
        }

        int nrOfProcessors = runtime.availableProcessors();
        String kernel = System.getProperty("os.name") + " " +
                        System.getProperty("os.version") + " " +
                        System.getProperty("os.arch");
        String cpu;
        if (cpu_freq > 0) {
            cpu = "cpu: " + runtime.availableProcessors() + " " + "freq: " + cpu_freq + "MHz " +
                  "usage: " + Integer.toString(cpuUsage.usage) + "%";
        } else {
            cpu = "cpu: " + runtime.availableProcessors() + " " +
                  "usage: " + Integer.toString(cpuUsage.usage) + "%";
        }
        String mem;
        if (memUsage.total > 0) {
            mem = "mem: " + Integer.toString(memUsage.free / 1024) + "MiB" +
                  " / " + Integer.toString(memUsage.total / 1024) + "MiB " +
                  "free: " + String.format("%.0f", memUsage.free * 100.0f / memUsage.total) + "%";
        } else {
            mem = "mem: evaluating...";
        }
        String battery = "battery: " + readFile("/sys/class/power_supply/battery/capacity") + "%";
        String currentTime = "date: " + df.format(new Date());

        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        updateViews.setTextViewText(R.id.widget_label,
                                    kernel + "\n" +
                                    cpu + "\n" +
                                    mem + "\n" +
                                    battery + "\n" +
                                    currentTime + "\n");
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }
}
