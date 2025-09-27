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

    public static void saveShiftTimings(Context context, String startingTime, String endingTime, String halfDay) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("startingTime", startingTime);
        editor.putString("endingTime", endingTime);
        editor.putString("halfDay", halfDay);

        editor.apply();
    }

    public static String getStartingTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("startingTime", null);
    }

    public static String getEndingTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("endingTime", null);
    }

    public static String getHalfDay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("halfDay", null);
    }




    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
