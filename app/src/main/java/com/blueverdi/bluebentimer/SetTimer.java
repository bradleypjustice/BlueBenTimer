package com.blueverdi.bluebentimer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

public class SetTimer extends Activity {

    int timerInSecs = 0;
    int mins;
    int secs;


    public void onSetButtonPressed(View v) {
        NumberPicker np = (NumberPicker)  findViewById(R.id.numberPickerMinutes);
        timerInSecs = (mins * 60) + secs;
        np = (NumberPicker)  findViewById(R.id.numberPickerSeconds);
        Intent i = getIntent();
        i.putExtra("timerInSecs", timerInSecs);
        setResult(RESULT_OK,i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);
        timerInSecs = getIntent().getIntExtra("timerInSecs",0);
        mins = timerInSecs/60;
        secs = timerInSecs % 60;
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPickerMinutes);
        np.setMaxValue(300);
        np.setWrapSelectorWheel(true);
        np.setValue(mins);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                mins = newVal;
            }
        });
        np = (NumberPicker) findViewById(R.id.numberPickerSeconds);
        np.setMaxValue(59);
        np.setWrapSelectorWheel(true);
        np.setValue(secs);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                secs = newVal;
            }
        });
    }

}

