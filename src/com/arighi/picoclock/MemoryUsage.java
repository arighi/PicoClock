package com.arighi.picoclock.widget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class MemoryUsage {
    private static final String LOG_TAG = "MemoryUsage";
    private RandomAccessFile memFile;

    public static int free = 0;
    public static int total = 0;

    public MemoryUsage() {
        try {
            memFile = new RandomAccessFile("/proc/meminfo", "r");
        }
        catch (FileNotFoundException e) {
            memFile = null;
            Log.e(LOG_TAG, "cannot open /proc/meminfo: " + e);
        }
    }

    public void close() throws IOException {
        memFile.close();
    }

    public void update() {
        if (memFile == null)
            return;
        try {
            memFile.seek(0);
            String line;
            while ((line = memFile.readLine()) != null) {
                String[] parts = line.split("[ ]+");
                if ("MemTotal:".equals(parts[0])) {
                    total = Integer.parseInt(parts[1], 10);
                } else if ("MemFree:".equals(parts[0])) {
                    free = Integer.parseInt(parts[1], 10);
                    /* this is the last statistic, just quit to optimize the loop */
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Ops: " + e);
        }
    }
}