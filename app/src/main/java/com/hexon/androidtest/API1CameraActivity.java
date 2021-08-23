package com.hexon.androidtest;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.hexon.androidtest.databinding.ActivityCameraBinding;

import java.util.List;
import java.util.Objects;

public class API1CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = API1CameraActivity.class.getSimpleName();
    private static final int MSG_START_PREVIEW = 1;
    ActivityCameraBinding mBinding;
    private Camera mCamera;
    private int mCameraNumber;
    private SurfaceHolder mHolder = null;
    private int mCameraId;
    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch (id) {
                case MSG_START_PREVIEW:
                    startPreview();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        mCameraNumber = Camera.getNumberOfCameras();
        if (mCameraNumber <= 0) {
            Log.e(TAG, "No camera.");
            finish();
        }

        mCameraId = 0;
        initView();
        initCamera();
    }

    private void initCamera() {
        mHolder = mBinding.surfaceCamera.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initView() {
        mBinding.btnCaptrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

        mBinding.btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraId == 0) {
                    mCameraId = 1;
                } else {
                    mCameraId = 0;
                }
                startPreview();
            }
        });
    }

    private void capture() {
        if (mCamera == null) {
            return;
        }

        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "camera:" + camera);
                mHandler.sendEmptyMessage(MSG_START_PREVIEW);
            }
        });
    }

    private void startPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }

        try {
            mCamera = Camera.open(mCameraId);
            setCameraDisplayOrientation(mCameraId, mCamera);
            if (mCamera != null) {
                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    List<String> focusModeList = parameters.getSupportedFocusModes();
                    for (String focusMode : focusModeList){//检查支持的对焦
                        Log.d(TAG, "focusMode:" + focusMode);
                        if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        }else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        } else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                    }
                    mCamera.setParameters(parameters);
                    mCamera.setPreviewDisplay(mHolder);
                    Log.d(TAG, "start preview");
                    mCamera.startPreview();
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            Log.d(TAG, "onAutoFocus " + success);
                        }
                    });
                } catch (Exception e) {
                    mCamera.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mCamera != null) {
                mCamera.release();
            }
            mCamera = null;
            Toast.makeText(this, "Camera " + mCameraId + "Open fail!", Toast.LENGTH_SHORT).show();
        }
    }

    public static int getDisplayRotation() {
        return 0;
    }

    public static void setCameraDisplayOrientation(int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation();
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        if (mCamera != null) {
            mCamera.release();
        }
    }
}
