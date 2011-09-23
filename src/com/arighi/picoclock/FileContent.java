package com.arighi.picoclock.widget;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.util.Log;

public class FileContent extends File {

    private static final String LOG_TAG = "PicoClock";

    public FileContent(String filename) {
        super(filename);
    }

    public String content() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(this));
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line);
            }
            buf.close();
        } catch (java.io.IOException e) {
            Log.e(LOG_TAG, "failed to read " + getName());
        }

        return text.toString();
    }
}
