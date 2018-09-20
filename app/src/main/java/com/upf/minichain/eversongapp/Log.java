package com.upf.minichain.eversongapp;

public final class Log {
    public static final String LOG_TAG = "AdriHellLog::";

    public static void l(String string) {
        android.util.Log.v(LOG_TAG, string);
    }
}
