package com.arighi.picoclock.widget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class CpuUsage {
    private static final String LOG_TAG = "CpuUsage";

    private RandomAccessFile statFile;
    private long lastTotal;
    private long lastIdle;

    public static int usage = 0;

    private void open() {
        try {
            statFile = new RandomAccessFile("/proc/stat", "r");
        }
        catch (FileNotFoundException e) {
            statFile = null;
            Log.e(LOG_TAG, "cannot open /proc/stat: " + e);
        }
    }

    public void close() throws IOException {
        statFile.close();
    }

    public void update() {
        if (statFile == null) {
            open();
            if (statFile == null)
                return;
        }
        try {
            statFile.seek(0);
            String cpuLine = statFile.readLine();
            String[] parts = cpuLine.split("[ ]+");

            if (!"cpu".equals(parts[0])) {
                throw new IllegalArgumentException("unable to get cpu line");
            }

            long idle = Long.parseLong(parts[4], 10);
            long total = 0;

            boolean head = true;
            for (String part : parts) {
                if (head) {
                    head = false;
                    continue;
                }
                total += Long.parseLong(part, 10);
            }

            long diffIdle = idle - lastIdle;
            long diffTotal = total - lastTotal;

            usage = (int)((float)(diffTotal - diffIdle) / diffTotal * 100);
            lastTotal = total;
            lastIdle = idle;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Ops: " + e);
        }
    }
}
