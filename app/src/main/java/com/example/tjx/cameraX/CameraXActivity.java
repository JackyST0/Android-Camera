package com.example.tjx.cameraX;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tjx.R;
import com.example.tjx.utils.Permission;

import java.io.File;

public class CameraXActivity extends AppCompatActivity {
    private Button photoButton;
    private TextureView textureView;
    private static final int REQUEST_CODE_CAMERA = 0x0B;
    boolean flag = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除顶部状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camerax);

        photoButton = findViewById(R.id.photoButton);
        textureView = findViewById(R.id.textureView);

        // 动态授权
        if (ActivityCompat.checkSelfPermission(CameraXActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(CameraXActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            flag = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {//相机、文件读写
            boolean flag = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                startCamera();
            } else {
                Log.e("Permission","授权失败！");
                // 授权失败，退出应用
                this.finish();
            }
        }
    }

    private void startCamera() {
        // 清楚所有绑定
        CameraX.unbindAll();

        // 预览
        // 计算屏幕参数:宽、高 、屏幕高宽比、尺寸
        int aspRatioW = textureView.getWidth(); // 预览View的宽
        int aspRatioH = textureView.getHeight(); // 预览View的高
        Size screen = new Size(aspRatioW, aspRatioH); // 屏幕尺寸
        // 通过PreviewConfig注入预览设置
        PreviewConfig config = new PreviewConfig.Builder()
                // 将相机镜头朝向后置摄像头
                .setLensFacing(CameraX.LensFacing.BACK)
                // 将预览的目标旋转设置为匹配当前设备的方向
                .setTargetRotation(textureView.getDisplay().getRotation())
                // 设置预览的目标分辨率为屏幕尺寸大小
                .setTargetResolution(screen)
                .build();

        // 根据预览配置生成预览对象，并设置预览回调（每更改一次画面都调用一次该回调函数）
        Preview preview = new Preview(config);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                Log.d("DEBUG","##### " + output);
                if (textureView.getParent() instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) textureView.getParent();
                    viewGroup.removeView(textureView);
                    viewGroup.addView(textureView, 0);

                    textureView.setSurfaceTexture(output.getSurfaceTexture());
                    updateTransform();
                }
            }
        });

//        CameraX.bindToLifecycle(this, preview);

        // 拍照
        // 通过ImageCaptureConfig注入预览设置
        ImageCaptureConfig captureConfig = new ImageCaptureConfig.Builder()
                // 将图像捕获的目标纵横比设置为 16:9
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                // 将图像捕获模式设置为最小延迟
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                // 将图像捕获的目标旋转设置为匹配当前设备的方向
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        ImageCapture imageCapture = new ImageCapture(captureConfig);
        photoButton.setOnClickListener((view) -> {
            final File file = new File(getExternalMediaDirs()[0], System.currentTimeMillis() + ".jpg");
            Log.d("DEBUG", "##### file path: " + file.getPath());
            imageCapture.takePicture(file, ContextCompat.getMainExecutor(getApplicationContext()), new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    Log.d("DEBUG", "##### onImageSaved: " + file.getPath());
                    // 屏幕输出照片存储路径
                    if ( Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    Toast.makeText(CameraXActivity.this, "save file path: " + file.getPath(), Toast.LENGTH_LONG).show();
                    Looper.loop();
                }

                @Override
                public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                    Log.d("DEBUG", "##### onError: " + message);
                }
            });

        });

        // 分析
        ImageAnalysisConfig analysisConfig = new ImageAnalysisConfig.Builder()
                // 使分析器收到最新可用的图像帧进行分析
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(analysisConfig);
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getApplicationContext()),
                new MyAnalyzer());

        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }

    /**
     * 更新相机预览, 用以保证预览方向正确
     */
    private void updateTransform() {
        Matrix matrix = new Matrix();

        int centerX = textureView.getWidth() / 2;
        int centerY = textureView.getHeight() / 2;

        int rotationDegrees = 0;

        switch (textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
        }

        matrix.postRotate(-rotationDegrees, centerX, centerY);
        textureView.setTransform(matrix);
    }

    /**
     * 自定义Analyzer类, 实现ImageAnalysis.Analyzer接口
     * anylyze()是每一帧画面的回调函数
     */
    private class MyAnalyzer implements ImageAnalysis.Analyzer {
        private long lastAnalyzedTimestamp = 0L;

        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {
            final Image img = image.getImage();
            if (img != null) {
                Log.d("DEBUG", img.getWidth() + "," + img.getHeight());
            }
        }
    }
}