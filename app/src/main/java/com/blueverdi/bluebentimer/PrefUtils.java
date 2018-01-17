package com.blueverdi.bluebentimer;

/**
 * Created by brad on 1/16/18.
 * Based on http://www.devexchanges.info/2016/08/the-principle-of-using-countdowntimer.html
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    private static final String EXPIRE_TIME = "timer_expires";
    private static final String TIMER_IN_SECS = "timer_in_secs";
    private SharedPreferences mPreferences;

    public PrefUtils(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public long getExpireTime() {
        return mPreferences.getLong(EXPIRE_TIME, 0);
    }

    public int getTimerInSecs() {
        return mPreferences.getInt(TIMER_IN_SECS, 0);
    }

    public void setTimerData(long expireTime, int timerInSecs) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(EXPIRE_TIME, expireTime);
        editor.putInt(TIMER_IN_SECS, timerInSecs);
        editor.apply();
    }
}