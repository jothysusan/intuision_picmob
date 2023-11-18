package com.picmob.android.utils;

import android.util.Log;

import androidx.annotation.NonNull;

public class LogCapture {

    public static void d(String str) {
        Log.isLoggable(str, Log.DEBUG);
        Log.d("Debug", str);
    }


    public static void e(String str, @NonNull String str2) {
        if (Log.isLoggable(str, Log.ERROR)) {
            Log.e(str, str2);
        }
    }


    public static void i(String str) {
        Log.isLoggable(str, Log.INFO);
    }


    public static void v(String str) {
        Log.isLoggable(str, Log.VERBOSE);
    }
}
