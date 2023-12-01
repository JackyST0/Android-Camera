package com.example.meterrecognition.imageView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.example.meterrecognition.R;
import com.example.meterrecognition.camera1.Camera1Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageView2Activity extends Activity  {

    private ImageView imageView1;

    private ImageView imageView2;

    private ImageView imageView3;

    private ImageView imageView4;

    private ImageView imageView5;

    private ImageView imageView6;

    private ImageView imageView7;

    private ImageView imageView8;

    private Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_imageview2);
        imageView1 = (ImageView) findViewById(R.id.view1);
        imageView2 = (ImageView) findViewById(R.id.view2);
        imageView3 = (ImageView) findViewById(R.id.view3);
        imageView4 = (ImageView) findViewById(R.id.view4);
        imageView5 = (ImageView) findViewById(R.id.view5);
        imageView6 = (ImageView) findViewById(R.id.view6);
        imageView7 = (ImageView) findViewById(R.id.view7);
        imageView8 = (ImageView) findViewById(R.id.view8);
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        List<File> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                File file = new File(imagePath);
                list.add(file);
            }
        }
        imageView1.setImageURI(Uri.fromFile(list.get(0)));
        imageView2.setImageURI(Uri.fromFile(list.get(1)));
        imageView3.setImageURI(Uri.fromFile(list.get(2)));
        imageView4.setImageURI(Uri.fromFile(list.get(3)));
        imageView5.setImageURI(Uri.fromFile(list.get(4)));
        imageView6.setImageURI(Uri.fromFile(list.get(5)));
        imageView7.setImageURI(Uri.fromFile(list.get(6)));
        imageView8.setImageURI(Uri.fromFile(list.get(7)));

        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到另一个名为
                Intent intent = new Intent(ImageView2Activity.this, Camera1Activity.class);
                startActivity(intent);
            }
        });
    }
}