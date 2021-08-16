package com.hexon.androidtest;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class IndicatorLightTestActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "IndicatorLightTest";
    private static final String INDICATOR_LIGHT_PATH = " proc/leds_proc";
    private static final String RED_LIGHT_ON_ORDER = "11";
    private static final String RED_LIGHT_OFF_ORDER = "10";
    private static final String GREEN_LIGHT_ON_ORDER = "21";
    private static final String GREEN_LIGHT_OFF_ORDER = "20";
    private static final String BLUE_LIGHT_ON_ORDER = "31";
    private static final String BLUE_LIGHT_OFF_ORDER = "30";

    CheckBox mCBRed, mCBGreen, mCBBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int isFullTest = getIntent().getIntExtra("isFullTest", 0);
        int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.indicator_light_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mCBRed = (CheckBox) findViewById(R.id.cb_red);
        mCBRed.setOnCheckedChangeListener(this);
        mCBGreen = (CheckBox) findViewById(R.id.cb_green);
        mCBBlue = (CheckBox) findViewById(R.id.cb_blue);

        turnOnOffRedLight(false);
        turnOnOffGreenLight(false);
        turnOnOffBlueLight(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.cb_red:
                turnOnOffRedLight(isChecked);
                break;
            case R.id.cb_green:
                turnOnOffGreenLight(isChecked);
                break;
            case R.id.cb_blue:
                turnOnOffBlueLight(isChecked);
                break;
        }
    }

    private void turnOnOffBlueLight(boolean isChecked) {
        writeFile(isChecked ? RED_LIGHT_ON_ORDER : RED_LIGHT_OFF_ORDER);
    }

    private void turnOnOffGreenLight(boolean isChecked) {
        writeFile(isChecked ? GREEN_LIGHT_ON_ORDER : GREEN_LIGHT_OFF_ORDER);
    }

    private void turnOnOffRedLight(boolean isChecked) {
        writeFile(isChecked ? BLUE_LIGHT_ON_ORDER : BLUE_LIGHT_OFF_ORDER);
    }

    public static boolean writeFile(String value){
        Log.i(TAG, "writeFile:" + value);
        if (value == null || value.length() <= 0) {
            Log.e(TAG, "Invalid cmd:" + value);
            return false;
        }
        try {
            FileWriter fileWriter = new FileWriter(INDICATOR_LIGHT_PATH);
            fileWriter.write(value);
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
