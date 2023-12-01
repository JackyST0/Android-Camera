package com.example.meterrecognition.imageView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.meterrecognition.R;
import com.example.meterrecognition.camera1.Camera1Activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author tjx
 * @date 2023/11/29 14:08
 */
public class ImageView3Activity extends Activity implements View.OnClickListener{

    private GridLayout imageLayout;

    private List<ImageBean> imageList = new ArrayList<>();

    private int index;

    private ImageButton cameraBtn;

    private String imagePath = null;

    private static final int REQUEST_CODE_CAMERA = 0x0A;

    private Handler handler = new Handler() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_imageview3);
        imageLayout = findViewById(R.id.imageLayout);
        cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(this);
        getUploadImageList();
        showUploadImage();


    }

    private void getUploadImageList() {
        //查询路径下的所有图片
        String path = getBaseContext().getExternalCacheDir().getAbsolutePath();
        File file2 = new File(path);
        File[] files = file2.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                    ImageBean imageBean = new ImageBean(null, files[i]);
                    imageList.add(imageBean);

            }
        }
    }

    private void showUploadImage() {
        for (int i = 0; imageList != null && i < imageList.size(); i++) {
            ImageBean imageBean = imageList.get(i);
//            String imageId = imageBean.getId().toString();
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
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new FileInputStream(imageFileName);
                //流转换多次读取
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                //前台显示
                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        return imageView;
    }

    @Override
    public void onClick(View v) {
        if (v == cameraBtn) {
            takePhoto();
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
            imageUri = FileProvider.getUriForFile(this, "android.support.v4.content.fileProvider", imageFile);
        } else {
            imageUri = Uri.fromFile(imageFile);
        }
        return imageUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA) {
            cameraCallback();
        }
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
            //质量压缩图片大小
            bitmap = ImageUtils.getLimitBitmap(bitmap, 500);
            File copyFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
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
            bitmap.recycle();
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
                }
            }
        }
        return null;
    }
}
