package com.upf.minichain.eversongapp;

public final class Log {
    public static final String LOG_TAG = "EversongAppLog::";

    public static void l(String string) {
        android.util.Log.v(LOG_TAG, string);
    }

    public static void e(String string) {
        android.util.Log.e(LOG_TAG, string);
    }
}
