package com.blueverdi.bluebentimer;

import android.os.Handler;
import android.os.Message;

/**
 * Created by brad on 1/10/18.
 */

class Timer {

    public interface TimerNotifier {
        void visualAlarmCompleted();
        void alarmCompleted();
        void updateCounter(String s);
        void updateHours(String s);
        void updateMinutes(String s);
        void updateSeconds(String s) ;
    }

    private class TimerBreakdown {
        public final long hours;
        public final long minutes;
        public final long seconds;

        TimerBreakdown(long secs) {
            hours = secs/3600;
            secs  = secs % 3600;
            seconds = secs % 60;
            minutes = secs/60;
        }
    }

    private static final String TAG = "Timer";
    private final long timerInSecs;
    private long timerExpires;
    private final TimerNotifier notifier;
    private UIUpdateHandler handler = null;
    private boolean timerActive;


    Timer(long timerInSecs, long timerExpires, TimerNotifier notifier) {
        MyLog.d(TAG, "creating timer, timerExpires: " + String.valueOf(timerExpires)
                + ", timerInSecs: " + String.valueOf(timerInSecs));
        this.timerInSecs = timerInSecs;
        this.timerExpires = timerExpires;
        this.notifier = notifier;

        updateTimerInfo(timerInSecs);
        if (timerExpires != 0) {
            long millisRemaining = (timerExpires - System.currentTimeMillis()) + 1000;
            if (millisRemaining <= 0) {
                timerActive = false;
                updateCounter(timerInSecs);
                notifier.visualAlarmCompleted();
            } else {
                timerActive = true;
                updateCounter(millisRemaining / 1000);
                handler = new UIUpdateHandler(this);
            }
        }
        else {
            updateCounter(timerInSecs);
        }
    }

    public long getTimerInSecs() {
        return timerInSecs;
    }

    public void setTimerExpires(long timerExpires) {
        this.timerExpires = timerExpires + 1000;
    }

    public long getTimerExpires() {
        return timerExpires;
    }

    public boolean getTimerActive() {
        return timerActive;
    }

    public void cancelTimer(){
        timerActive = false;
        timerExpires = 0;
    }

    public void startTimer() {
        timerActive = true;
        handler = new UIUpdateHandler(this);
    }

    private void soundAlarm() {
        notifier.alarmCompleted();
    }

    private void updateCounter(long seconds) {
        TimerBreakdown tb = new TimerBreakdown(seconds);
        StringBuilder sb = new StringBuilder();
        if (tb.hours < 10) {
            sb.append("0");
        }
        sb.append(tb.hours);
        sb.append(":");
        if (tb.minutes < 10) {
            sb.append("0");
        }
        sb.append(tb.minutes);
        sb.append(":");
        if (tb.seconds < 10) {
            sb.append("0");
        }
        sb.append(tb.seconds);
        String s = sb.toString();
        notifier.updateCounter(s);
    }

    private void resetCounter() {
        updateCounter(timerInSecs);
    }
    private void updateTimerInfo(long timerInSecs) {
        TimerBreakdown tb = new TimerBreakdown(timerInSecs);
        notifier.updateHours(String.valueOf(tb.hours));
        notifier.updateMinutes(String.valueOf(tb.minutes));
        notifier.updateSeconds(String.valueOf(tb.seconds));
    }

    private long timerExpires() {
        return timerExpires;
    }
    static class UIUpdateHandler extends Handler {

        private final static int UPDATE_RATE_MS = 1000;
        private final static int MSG_UPDATE_TIME = 0;
        private final Timer timer;

        UIUpdateHandler(Timer timer)
        {
            this.timer = timer;
            sendEmptyMessage(MSG_UPDATE_TIME);
        }

        @Override
        public void handleMessage(Message message) {
            if (MSG_UPDATE_TIME == message.what) {
                MyLog.d(TAG, "updating time");
            }
            if (timer.timerActive) {
                long millisRemaining = timer.timerExpires() - System.currentTimeMillis();
                if (millisRemaining <= 0) {
                    timer.soundAlarm();
                    timer.timerActive = false;
                    timer.timerExpires = 0;
                    timer.resetCounter();
                } else {
                    timer.updateCounter(millisRemaining / 1000);
                }
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS);
            }
            else {
                MyLog.d(TAG, "handler: timer cancelled");
                timer.resetCounter();
            }
        }
    }

}
