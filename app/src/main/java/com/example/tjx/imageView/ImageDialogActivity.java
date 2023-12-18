package com.example.tjx.imageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;


import com.example.tjx.R;
import com.example.tjx.utils.UIUtils;

import java.io.File;
import java.io.FileInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ImageDialogActivity extends Activity {

    private Unbinder unbinder;

    ImageView imageDialog;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_dialog);
        unbinder = ButterKnife.bind(this);
        imageDialog = findViewById(R.id.image_dialog);

        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            showImage();
        }
        imageDialog.setOnClickListener(v -> {
            //单击关闭弹窗
            finish();
        });
    }

    private void showImage() {
        try {
            File imageFile = new File(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            imageDialog.setImageBitmap(bitmap);
        } catch (Exception e) {
            UIUtils.showToastFail("图片显示错误");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
