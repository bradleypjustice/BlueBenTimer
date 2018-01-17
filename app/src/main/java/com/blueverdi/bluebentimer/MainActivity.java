package com.blueverdi.bluebentimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity{
    public static final int SET_TIMER = 1;
    private static final String TAG = "MainActivity";
    private Timer timer;
    private int timerInSecs;
    private PrefUtils prefUtils;

    private class MyTimerNotifier implements Timer.TimerNotifier {
        private final WeakReference<MainActivity> activity;

        MyTimerNotifier(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        public void visualAlarmCompleted()  {
            prefUtils.setTimerData(0,timerInSecs);
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getString(R.string.app_name));
                    alertDialog.setMessage(getString(R.string.timer_complete));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }}));
            prefUtils.setTimerData(0,timerInSecs);
            MyLog.d(TAG, "displayed alarm complete");
        }

        public void alarmCompleted() {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    MediaPlayer ring= MediaPlayer.create(MainActivity.this,R.raw.hourlychimebeg);
                    ring.start();

                }
            };
            thread.start();
            visualAlarmCompleted();
            prefUtils.setTimerData(0,timerInSecs);
        }

        public void updateCounter(final String s) {
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    TextView tv = (TextView) findViewById(R.id.counter);
                    tv.setText(s);
                }}));
        }

        public void updateHours(final String s) {
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    TextView tv = (TextView) findViewById(R.id.hours);
                    tv.setText(String.valueOf(s));
                }}));
        }

        public void updateMinutes(final String s) {
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    TextView tv = (TextView) findViewById(R.id.minutes);
                    tv.setText(String.valueOf(s));
                }}));
        }

        public void updateSeconds(final String s) {
            runOnUiThread (new Thread(new Runnable() {
                public void run() {
                    TextView tv = (TextView) findViewById(R.id.seconds);
                    tv.setText(String.valueOf(s));
                }}));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new PrefUtils(getApplicationContext());
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.d(TAG,"onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initializing a countdown timer
        removeAlarmManager();
        timerInSecs = prefUtils.getTimerInSecs();
        long expireTime = prefUtils.getExpireTime();
        if (timerInSecs != 0) {
            timer = new Timer((long) timerInSecs, expireTime, new MyTimerNotifier(this));
            if (expireTime >= System.currentTimeMillis()) {
                timer.startTimer();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        boolean timing = false;
        long timerExpires = 0;
        try {
            timing = timer.getTimerActive();
            timerExpires = timer.getTimerExpires();
            timer.cancelTimer();
        }
        catch (Exception e) {

        }
        prefUtils.setTimerData(timerExpires,timerInSecs);
        if (timing) {
            setAlarmManager(timerExpires);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onButtonPressed(View v) {
        switch(v.getId()) {
            case R.id.set_button:
                Intent pickContactIntent = new Intent(this, SetTimer.class);
                pickContactIntent.putExtra("timerInSecs", timerInSecs);
                startActivityForResult(pickContactIntent, SET_TIMER);
                break;

            case R.id.start_button:
                if (timerInSecs == 0) {
                    Toast.makeText(this, getText(R.string.zero_time), Toast.LENGTH_SHORT).show();
                    return;
                }
                long timerExpires = System.currentTimeMillis() + (timerInSecs * 1000);
                timer.setTimerExpires(timerExpires);
                timer.startTimer();
                break;

            case R.id.stop_button:
                prefUtils.setTimerData(0,timerInSecs);
                try {
                    timer.cancelTimer();
                }
                catch (Exception e) {
                    MyLog.d(TAG, "exception calling handler.stopTimer");
                }
                break;

            case R.id.about:
                Intent aboutIntent = new Intent(this,AboutActivity.class);
                startActivity(aboutIntent);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SET_TIMER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                timerInSecs = data.getIntExtra("timerInSecs", timerInSecs);
//                long timerExpires = System.currentTimeMillis() + (timerInSecs * 1000);
                prefUtils.setTimerData(0, timerInSecs);
                timer = new Timer((long) timerInSecs, 0, new MyTimerNotifier(this));
            }
        }
    }


    public void setAlarmManager(long timerExpires) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TimeReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(timerExpires, sender), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, timerExpires, sender);
        }
    }

    public void removeAlarmManager() {
        Intent intent = new Intent(this, TimeReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}