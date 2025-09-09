package com.developer.attd.Utils;

import android.content.Context;
import android.content.SharedPreferences;
public class PrefsUtils {

    private static final String PREFS_NAME = "attendance_prefs";

    public static void saveAttendanceStatus(Context context, String attdStatus) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("attd_status", attdStatus).apply();
    }

    public static String getAttendanceStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("attd_status", "Unknown");
    }

    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
