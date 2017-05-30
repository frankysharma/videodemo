package com.rahul.video.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.rahul.video.R;

/**
 * Created by rahul on 27-05-2017.
 */

public class Utils {

    public static void toast(Context context, String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    /**
     * Save String in Shared Preferences.
     *
     * @param context The context of activity
     * @param key Key to save value
     * @param value value to save
     *
     */
    public static void savePreferences(Context context, String key, String value) {
        SharedPreferences mPreferences = context.getSharedPreferences(String.valueOf(R.string.app_name), context.MODE_PRIVATE);
        mPreferences.edit().putString(key, value).apply();
    }

    /**
     * Delete from Shared Preferences.
     *
     * @param context The context of activity
     * @param key Key to save value
     *
     */
    public static void deletePreferences(Context context, String key) {
        SharedPreferences mPreferences = context.getSharedPreferences(String.valueOf(R.string.app_name), context.MODE_PRIVATE);
        mPreferences.edit().remove(key).apply();
    }

    /**
     * Get String from Shared Preferences.
     *
     * @param context The context of activity
     * @param key Key to get value
     *
     * @return String
     */
    public static String getPrefrences(Context context, String key){
        SharedPreferences mPreferences = context.getSharedPreferences(String.valueOf(R.string.app_name), context.MODE_PRIVATE);
        String result = mPreferences.getString(key, "");
        return result;
    }
}
