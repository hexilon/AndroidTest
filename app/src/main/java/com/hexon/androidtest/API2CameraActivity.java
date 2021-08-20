package com.hexon.androidtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.hexon.androidtest.databinding.ActivityCameraBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class API2CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = API2CameraActivity.class.getSimpleName();
    ActivityCameraBinding mBinding;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder = null;
    CameraManager mManager;
    private String mCameraBackId;
    private String mCameraFrontId;
    private String mCurrCameraId;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mCaptureImageReader;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private Surface mPreviewSurface;
    private Size mPreviewSize;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    ImageReader.OnImageAvailableListener mCaptureImageListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                }
            };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session,
                                             CaptureRequest request, long timestamp,
                                             long frameNumber) {
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session,
                                                CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraThread = new HandlerThread("OneCamera2");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        initView();
        initCamera();
    }

    private void initView() {
        mBinding.btnCaptrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mBinding.btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrCameraId.equals(mCameraBackId)) {
                    mCurrCameraId = mCameraFrontId;
                } else {
                    mCurrCameraId = mCameraBackId;
                }
                openCamera();
            }
        });
    }

    private void initCamera() {
        try {
            String[] ids = mManager.getCameraIdList();

            for (String id : ids) {
                CameraCharacteristics characteristics = mManager.getCameraCharacteristics(id);
                int face = characteristics.get(CameraCharacteristics.LENS_FACING);
                String info = characteristics.get(CameraCharacteristics.INFO_VERSION);
                Log.d(TAG, "info:" + info);
                if (face == CameraMetadata.LENS_FACING_FRONT) {
                    Log.d(TAG, id + " is front camera");
                    mCameraFrontId = id;
                } else {
                    mCameraBackId = id;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mCurrCameraId = mCameraBackId;
    }

    private boolean checkFrontCameraExists() {
        try {
            String[] ids = mManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics characteristics = mManager.getCameraCharacteristics(id);
                String info = characteristics.get(CameraCharacteristics.INFO_VERSION);
                Log.d(TAG, "info:" + info);
                if (info.equals("2")) {
                    Log.d(TAG, id + " is back Auxiliary camera");
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void takePicture() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        openCamera();
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            if (mCurrCameraId == null) {
                return;
            }
            CameraCharacteristics characteristics = mManager.getCameraCharacteristics(mCurrCameraId);
            String info = characteristics.get(CameraCharacteristics.INFO_VERSION);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mPreviewSize = getOptimalPreviewSize(Arrays.asList(sizes.clone()), dm.widthPixels, dm.heightPixels);
            Log.d(TAG, "mPreviewSize:" + mPreviewSize);
            mHolder.setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mManager.openCamera(mCurrCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "opened");
                    mCameraDevice = camera;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.d(TAG, "onDisconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.d(TAG, "onError:" + error);
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        Log.d(TAG, "getOptimalPreviewSize w:" + w + " h:" + h);
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            Log.d(TAG, "getOptimalPreviewSize w:" + size.getWidth() + " h:" + size.getHeight());
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            int tempDiff = Math.abs(size.getHeight() - targetHeight);
            if (tempDiff < minDiff) {
                optimalSize = size;
                minDiff = tempDiff;
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mCaptureImageReader) {
            mCaptureImageReader.close();
            mCaptureImageReader = null;
        }

        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            mPreviewSurface = mHolder.getSurface();

            mCaptureImageReader = ImageReader.newInstance(mPreviewSize.getWidth(),
                    mPreviewSize.getHeight(), ImageFormat.JPEG, 2);
            mCaptureImageReader.setOnImageAvailableListener(mCaptureImageListener, mCameraHandler);
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mPreviewSurface);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(mPreviewSurface);
            outputSurfaces.add(mCaptureImageReader.getSurface());
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(outputSurfaces,
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            repeatingPreview();
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, mCameraHandler
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private boolean repeatingPreview() {
        try {
            CaptureRequest.Builder builder = mCameraDevice.
                    createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mPreviewSurface);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mCaptureSession.setRepeatingRequest(builder.build(), mCaptureCallback,
                    mCameraHandler);
            return true;
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Could not access camera setting up preview.", ex);
            return false;
        }
    }
}
