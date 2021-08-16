package com.hexon.androidtest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class UartActivity extends Activity {
    private static final String TAG = "UartActivity";
    private static final int DEFAULT_BAUD_RATE = 115200;
    private static final String DEFAULT_SEND = "Uart Test";
    private static final String UART_PREFIX = "ttyS";
    private static final String DEV_PATH = "/dev";
    Spinner mBaudRateSpinner;
    TextView mTvLog;
    Boolean[] mTestResult;

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            getString(R.string.app_name);
        }
    };
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        initView();
        mTestResult = new Boolean[1];
    }

    private void initView() {
        Spinner pathSpinner = findViewById(R.id.spinner_serial_port);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                getAllUartDevicesPath());
        pathSpinner.setAdapter(adapter);

        mBaudRateSpinner = findViewById(R.id.spinner_baud_rate);
        for (int i = 0; i < mBaudRateSpinner.getAdapter().getCount(); i++) {
            if (Integer.parseInt((String) (mBaudRateSpinner.getAdapter().getItem(i)))
                    == DEFAULT_BAUD_RATE) {
                mBaudRateSpinner.setSelection(i);
                break;
            }
        }

        Button btnTest = findViewById(R.id.btn_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUartTest();
            }
        });

        mTvLog = findViewById(R.id.tv_log);
        mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void startUartTest() {
        mCount ++;
        String test = "abcdefg";
        Log.d(TAG, test.substring(0, test.length() -1));
        int baudRate = Integer.parseInt(mBaudRateSpinner.getSelectedItem().toString());
        mTvLog.append("test" + mCount + "\n");
        int offset = mTvLog.getLineCount() * mTvLog.getLineHeight();
        if (offset > mTvLog.getHeight()) {
            mTvLog.getScrollY();
            mTvLog.scrollTo(0,offset - mTvLog.getHeight()/* + mTvLog.getLineHeight()*/);
        }

    }

    public String[] getAllUartDevicesPath() {
        Vector<String> devices = new Vector<String>();
        File devFile = new File(DEV_PATH);
        if (devFile.exists() && devFile.canRead()) {
            for (File file : devFile.listFiles()) {
                if (file.getName().contains(UART_PREFIX)) {
                    Log.d(TAG, file.getName());
                    devices.add(file.getAbsolutePath());
                }
            }
        }

        return devices.toArray(new String[devices.size()]);
    }

    private static class UartGpioControl {
        static ArrayList<GpioConfig> mUartGpioList = new ArrayList<>(Arrays.asList(
                new GpioConfig("/dev/ttyS1", 112, 1, 0)
        ));
        public static boolean openUart(String uart) {
            for (GpioConfig gpioConfig : mUartGpioList) {
                if (gpioConfig.uart.equals(uart)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean closeUart(String uart) {
            for (GpioConfig gpioConfig : mUartGpioList) {
                if (gpioConfig.uart.equals(uart)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class GpioConfig {
        String uart;
        int gpio;
        int open;
        int close;

        public GpioConfig(String uart, int gpio, int open, int close) {
            this.uart = uart;
            this.gpio = gpio;
            this.open = open;
            this.close = close;
        }
    }
}
