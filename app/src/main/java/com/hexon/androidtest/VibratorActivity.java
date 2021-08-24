package com.hexon.androidtest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.hexon.androidtest.databinding.ActivityVibratorBinding;

public class VibratorActivity extends Activity {
    ActivityVibratorBinding mDataBinding;
    Vibrator mVibrator;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_vibrator);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator.hasVibrator()) {
            mDataBinding.layoutSupport.setVisibility(View.VISIBLE);
            mDataBinding.layoutNotSupport.setVisibility(View.GONE);
        } else {
            mDataBinding.layoutSupport.setVisibility(View.GONE);
            mDataBinding.layoutNotSupport.setVisibility(View.VISIBLE);
        }

        mDataBinding.btnVibrator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVibrator.vibrate(1000);
            }
        });
    }
}
