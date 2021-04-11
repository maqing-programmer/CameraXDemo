package com.example.cameraxdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by maqing on on 2021/4/11.
 * Email:2856992713@qq.com
 * 视频录制
 */
public class VideoRecodeActivity extends AppCompatActivity {
    private PreviewView mPreviewView;
    private Button mTakePhotoBtn;
    private Button mStartBtn;
    private Button mStopBtn;
    private VideoCapture mVideoCapture;
    private ProcessCameraProvider mProcessCameraProvider;
    private Preview mPreview;
    private Activity mActivity;
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_PERMISSIONS = 1001;
    private boolean mIsRecording;
    private static final String TAG = "VideoRecodeActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recode);
        mActivity = this;
        mTakePhotoBtn = findViewById(R.id.btn_activity_video_recode_take);
        mPreviewView = findViewById(R.id.preview_activity_video_recode);
        mStartBtn = findViewById(R.id.btn_activity_video_recode_start);
        mStopBtn = findViewById(R.id.btn_activity_video_recode_stop);
        initEvent();
        startPreview();
    }

    private void initEvent() {

        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsRecording) {
                    mIsRecording = true;
                    startRecorder();
                }
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    stopRecorder();
                    mIsRecording = false;
                }
            }
        });
    }

    private void startPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            } else if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            } else if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            } else if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_PERMISSIONS);
            } else {
                preview();
            }
        } else {
            preview();
        }
    }

    @SuppressLint("RestrictedApi")
    private void preview() {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(mActivity);
        try {
            mProcessCameraProvider = listenableFuture.get();
            mProcessCameraProvider = listenableFuture.get();

            mPreview = new Preview.Builder()
                    .setTargetResolution(new Size(640, 480))
                    .build();
            mPreview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .build();
            imageAnalysis
                    .setAnalyzer(ContextCompat.getMainExecutor(mActivity),
                            new ImageAnalysis.Analyzer() {
                                @Override
                                public void analyze(@NonNull ImageProxy image) {
                                    Log.e(TAG, "analyze: " + image);
                                    image.close();
                                }
                            }
                    );
            mProcessCameraProvider.bindToLifecycle((LifecycleOwner) mActivity, CameraSelector.DEFAULT_BACK_CAMERA,
                    mPreview,
                    imageAnalysis
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void takePhoto() {
        ImageCapture imageCapture = new ImageCapture.Builder()
                //优化捕获速度，可能降低图片质量
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                //设置宽高比
                .setTargetResolution(new Size(640, 480))
                //设置初始的旋转角度
                .build();
        String dirPath = getExternalFilesDir("").getAbsolutePath()
                + File.separator + "TestRecode";
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            boolean mkdir = dirFile.mkdir();
            Log.e(TAG, "takePhoto: mkdir：" + mkdir);
        }
        File file = new File(dirFile, System.currentTimeMillis() + ".jpg");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.e(TAG, "takePhoto: newFile：" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();


        mProcessCameraProvider.bindToLifecycle((LifecycleOwner) mActivity, CameraSelector.DEFAULT_BACK_CAMERA,
                imageCapture
        );

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(mActivity), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.e(TAG, "onImageSaved: " + outputFileResults);
                Uri savedUri = outputFileResults.getSavedUri();
                Log.e(TAG, "onImageSaved: " + savedUri);
                if (savedUri == null) {
                    savedUri = Uri.fromFile(file);
                }
                Toast.makeText(mActivity, "拍摄成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "onError: " + exception);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void startRecorder() {

        mVideoCapture = new VideoCapture.Builder()
                .build();

        String dirPath = getExternalFilesDir("").getAbsolutePath()
                + File.separator + "TestRecode";
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            boolean mkdir = dirFile.mkdir();
            Log.e(TAG, "startRecorder: mkdir：" + mkdir);
        }
        File file = new File(dirFile, System.currentTimeMillis() + ".mp4");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.e(TAG, "startRecorder: newFile：" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mProcessCameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, mPreview,
                mVideoCapture);

        mVideoCapture.startRecording(file, ContextCompat.getMainExecutor(mActivity),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull File file) {
                        Log.e(TAG, "onVideoSaved: " + file);
                        Toast.makeText(mActivity, "录制结束", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        Log.e(TAG, "onError: " + videoCaptureError + "," + message);
                        Toast.makeText(mActivity, "录制出错：code：" + videoCaptureError + "," + message, Toast.LENGTH_SHORT).show();
                        mIsRecording = false;
                    }
                }
        );
    }

    @SuppressLint("RestrictedApi")
    private void stopRecorder() {
        if (mVideoCapture != null) {
            mVideoCapture.stopRecording();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mActivity, "请到设置中打开应用的相机权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mActivity, "请到设置中打开应用的录音权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mActivity, "请到设置中打开应用的存储读权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mActivity, "请到设置中打开应用的存储写权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                preview();
                break;
        }
    }
}
