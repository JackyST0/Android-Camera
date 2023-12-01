package com.example.tjx.camera1;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tjx.R;
import com.example.tjx.utils.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author tjx
 * @date 2023/11/24 9:03
 */
public class Camera1Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    /*
     * 创建SurfaceHolder,Camera,SurfaceViiew;
     * */
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private SurfaceView surfaceView;

    private File picturesPath = null;

    private int orientation;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除顶部状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera1);
        init();
    }

    private void init(){
        Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true){
                    if(!Permission.isPermissionGranted(Camera1Activity.this)){
                        continue;
                    }
                    // 找到对应布局id，通过getHolder()方法获得当前SurfaceView的SurfaceHolder对象，然后对SurfaceHolder对象添加回调来监听Surface的状态
                    surfaceView = findViewById(R.id.camera_surface_view);
                    findViewById(R.id.camera_take_picture).setOnClickListener(Camera1Activity.this);
                    surfaceHolder = surfaceView.getHolder();

                    surfaceHolder.addCallback(new SurfaceHolder.Callback() {

                        // surface第一次创建时回调
                        @Override
                        public void surfaceCreated(@NonNull SurfaceHolder holder) {
                            camera = Camera.open(cameraId);

                            try {
                                // 获取摄像头支持的宽、高
                                Parameters parameters = camera.getParameters();
                                List<Size> sizes = parameters.getSupportedPreviewSizes();

                                if (sizes != null) {
                                    Size size  = sizes.get(0);
                                    parameters.setPreviewSize(size.width,size.height);
                                    parameters.setPreviewFormat(ImageFormat.NV21);
                                    camera.setParameters(parameters);
                                }

                                // 将相机的预览数据输出到指定的表面对象上
                                camera.setPreviewDisplay(holder);
                                // 将预览方向顺时针旋转90度
                                camera.setDisplayOrientation(90);
                                // 启动相机预览
                                camera.startPreview();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // surface变化的时候回调
                        @Override
                        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                        }

                        // surface销毁的时候回调
                        @Override
                        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                            releaseCamera();
                        }
                    });
                    break;
                }
            }
        });
        t.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permission.checkPermission(Camera1Activity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Permission.isPermissionGranted(this)) {
            Log.i("PERMISSION","请求权限成功");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    private void releaseCamera() {
        if (null != camera) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Permission.REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission","授权失败！");
                    // 授权失败，退出应用
                    this.finish();
                    return;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        picturesPath = getExternalCacheDir();

        if (v.getId() == R.id.camera_take_picture) {
            if (null != camera) {
                camera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        //base data
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] data, Camera camera) {
                        camera.startPreview();

                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        savePhoto(data);
                                    }
                                }
                        ).start();
                    }
                });
            }
        }
    }

    private void savePhoto(final byte[] data) {
        if (!picturesPath.exists()) {
            picturesPath.mkdirs();
        }

        File file = new File(picturesPath, System.currentTimeMillis() + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        rotateImageView(cameraId, orientation, file.getPath());
    }

    private void rotateImageView(int cameraId, int orientation, String path) {
        Log.d("DEBUG", "##### save path: " + path);
        // 将照片文件解码为Bitmap对象
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        // 声明一个Bitmap对象来存储旋转后的位图
        Bitmap resizeBitmap = null;
        // 根据相机镜头处理位图，利用Matrix类来旋转位图
        switch (cameraId) {
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                Matrix matrix = new Matrix();
                if (orientation == 90) {
                    matrix.postRotate(90);
                }
                resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                Matrix m = new Matrix();
                m.postScale(-1f, 1f);
                m.postRotate(90);
                resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        m, true);
                break;
        }

        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            resizeBitmap.recycle();


        } catch (Exception e) {
            e.printStackTrace();
        }

        Looper.prepare();
        Toast.makeText(Camera1Activity.this, "save file path: " + path, Toast.LENGTH_LONG).show();
        Looper.loop();
    }
}
