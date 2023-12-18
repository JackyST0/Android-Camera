package com.example.tjx.imageView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.tjx.R;
import com.example.tjx.utils.FileUtils;
import com.example.tjx.utils.ImageBean;
import com.example.tjx.utils.ImageUtils;
import com.example.tjx.utils.UIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author tjx
 * @date 2023/11/29 14:08
 */
public class ImageView3Activity extends Activity implements View.OnClickListener{

    private GridLayout imageLayout;
    private List<ImageBean> imageList = new ArrayList<>();
    private int index;
    ImageButton cameraSelectBtn;
    private ImageButton cameraBtn;
    private String imagePath = null;
    private final int GET_PLATFORMAREA_SUCCESS = 2;
    private final int GET_PLATFORMAREA_FAIL = 3;
    private static final int REQUEST_CODE_SETTING = 0x01;
    private static final int REQUEST_CODE_CAMERA = 0x0B;
    private static final int REQUEST_CHOOSE_PHOTO = 500;
    private int cameraOperation;//照相机操作类型
    private String DOWNLOAD_IMAGE_FILE_PATH = FileUtils.getCachePath() + File.separator + "downloadImageData";//服务器下载图片的路径

   Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PLATFORMAREA_SUCCESS:
                    getUploadImageList();
                    break;
                case GET_PLATFORMAREA_FAIL:
                    UIUtils.showToastFail("获取数据失败");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_imageview3);
        imageLayout = findViewById(R.id.imageLayout);
        cameraBtn = findViewById(R.id.cameraBtn);
        cameraSelectBtn = findViewById(R.id.cameraSelectBtn);
        cameraBtn.setOnClickListener(this);
        cameraSelectBtn.setOnClickListener(this);
//        cameraSelectBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        getPlatformAreaInfo();
    }

    private void getPlatformAreaInfo() {
        new Thread(() -> {
            try {
                Message msg = new Message();
                msg.what = GET_PLATFORMAREA_SUCCESS;
                handler.sendMessage(msg);
            } catch (Exception e) {
                Log.e("getPlatformAreaInfo", "获取数据失败", e);
                Message msg = new Message();
                msg.what = GET_PLATFORMAREA_FAIL;
                msg.obj = "获取数据失败";
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void getUploadImageList() {
        new Thread(() -> {
        //查询路径下的所有图片
        String path = DOWNLOAD_IMAGE_FILE_PATH;
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                    ImageBean imageBean = new ImageBean(null, files[i]);
                    imageList.add(imageBean);
            }
        }
        showUploadImage();
        }).start();
    }


    private void showUploadImage() {
        index = 0;
        imageLayout.removeAllViews();
        if (imageList.isEmpty()) {
            return;
        }
        for (int i = 0; imageList != null && i < imageList.size(); i++) {
            ImageBean imageBean = imageList.get(i);
            //下载缩略图
            DownLoadImageTask downLoadImageTask = new DownLoadImageTask(null, imageBean.getFile().getAbsolutePath(), index++, imageLayout);
            downLoadImageTask.execute();
        }
    }

    private class DownLoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        private String imageId;
        private String imageFileName;
        private int imageIndex;

        private ImageView imageView;
        private GridLayout imageLayout;

        public DownLoadImageTask(String imageId, String imageFileName, int imageIndex, GridLayout imageLayout) {
            this.imageId = imageId;
            this.imageFileName = imageFileName;
            this.imageIndex = imageIndex;
            this.imageLayout = imageLayout;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            imageView = getImageView(bitmap, imageLayout, imageIndex, imageFileName);
            imageLayout.addView(imageView);
        }


        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = getImageBitmap(imageFileName);

            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        }
    }

    private ImageView getImageView(Bitmap bitmap, GridLayout imageLayout, int index, String imagePath) {

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);

        //使用Spec定义子控件的位置和比重
        GridLayout.Spec rowSpec = GridLayout.spec(index / 4, 1);
        GridLayout.Spec columnSpec = GridLayout.spec(index % 4, 1);
        //将Spec传入GridLayout.LayoutParams并设置宽高为0，必须设置宽高，否则视图异常
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        //计算4张图片宽度
        int width = imageLayout.getWidth();//获取宽度
        int childWidth = (width - 10 * 4) / 4;//减去边距再除4
        layoutParams.height = childWidth;
        layoutParams.width = childWidth;
        layoutParams.setMargins(5, 5, 5, 5);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        //点击预览大图
        imageView.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ImageDialogActivity.class);
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
        });
        //图片长按删除
        imageView.setLongClickable(true);
        imageView.setOnLongClickListener(view -> {
            final String[] toolbarArr = new String[]{"删除"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(toolbarArr, (dialog, i) -> {
                switch (i) {
                    case 0://删除
                        try {
                            removeGridImage(index);
                        } catch (Exception e) {
                            UIUtils.showToastFail("删除图片错误");
                            Log.e("getImageView", "删除图片错误", e);
                        }
                        break;
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        });
        return imageView;

    }

    /**
     * 删除表格中图片
     */
    private void removeGridImage(int i) {
        //删除当前图片
        ImageBean imageBean = imageList.get(i);
        imageList.remove(i);
        imageLayout.removeAllViews();
        if (imageBean.getFile() != null) {
            if (imageBean.getFile().isFile()) {
                imageBean.getFile().delete();
                UIUtils.showToastSuccess("删除图片成功");
            }
        }
        //重新加载图片。
        showUploadImage();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(cameraBtn)) {
            cameraPermission(REQUEST_CODE_CAMERA);
        } else if (v.equals(cameraSelectBtn)) {
            cameraPermission(REQUEST_CHOOSE_PHOTO);
        }
    }

    private void cameraPermission(int cameraOperation) {
        this.cameraOperation = cameraOperation;
        boolean flag = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            flag = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
            }
        }
        if (flag) {
            startCamera();
        }
    }

    private void startCamera() {
        switch (cameraOperation) {
            case REQUEST_CODE_CAMERA:
                takePhoto();
                break;
            case REQUEST_CHOOSE_PHOTO:
                ImageUtils.startChooseActivity(this, REQUEST_CHOOSE_PHOTO);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("权限")
                        .setMessage("权限被拒绝，是否进入设置界面手动授权：\n相机权限\n读写手机权限")
                        .setPositiveButton("设置", (dialog, which) -> {
                            //进入APP设置界面，手动打开权限
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_CODE_SETTING);
                        }).setNegativeButton("取消", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private void takePhoto() {
        File imageFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (!imageFile.exists()) {
            try {
                imageFile.createNewFile();
            } catch (Exception e) {
                Log.e("Exception", e.toString());
            }
        }
        imagePath = imageFile.getAbsolutePath();
        Uri imageUri = getImageUrl(imageFile);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private Uri getImageUrl(File imageFile) {
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(ImageView3Activity.this, "com.example.tjx.fileProvider", imageFile);
        } else {
            imageUri = Uri.fromFile(imageFile);
        }
        return imageUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHOOSE_PHOTO:
                    choosePhotoCallback(data);
                    break;
                case REQUEST_CODE_CAMERA:
                    cameraCallback();
                    break;
            }
        }
    }

    private void choosePhotoCallback(Intent data) {
        imagePath = FileUtils.getChooseFileResultPath(data.getData());
        //拷贝，防止操作源文件
        String imageName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".jpg";
        File copyFile = new File(getExternalCacheDir(), imageName);
        FileUtils.copyFile(imagePath, copyFile.getAbsolutePath(), false);
        imagePath = copyFile.getAbsolutePath();
        cameraCallback();
    }

    private void cameraCallback() {
        try {
            //手机照相回调，需要旋转
            ImageBean imageTemp = new ImageBean();
            File imageFileTemp = new File(imagePath);
            Bitmap bitmap = getImageBitmap(imagePath);
            int degree = ImageUtils.getBitmapDegree(imageFileTemp.getAbsolutePath());
            if (degree > 0) {//判断方向
                bitmap = ImageUtils.rotateBitmapByDegree(bitmap, degree);
            }

            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            File copyFile = new File(DOWNLOAD_IMAGE_FILE_PATH, System.currentTimeMillis() + ".jpg");
            ImageUtils.saveImageBitmap(copyFile, bitmap);
            imageFileTemp.delete();//删除旧横向照片
            imageTemp.setFile(copyFile);
            imagePath = copyFile.getAbsolutePath();
            imageList.add(imageTemp);
            //动态添加图片
            ImageView imageView = getImageView(bitmap, imageLayout, index++, imagePath);
            handler.postDelayed(() -> {
                    imageLayout.addView(imageView);
            }, 100);
        } catch (Exception e) {
            Log.e("camera", "显示照片错误", e);
        }
    }

    private Bitmap getImageBitmap(String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("getImageBitmap", "获取图像位图错误", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
