package com.example.tjx.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 鉴权类
 * @author tjx
 * @date 2023/11/24 10:21
 */
public class Permission {

    public static final int REQUEST_CODE = 5;
    // 定义三个权限
    private static final String[] permission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    // 每个权限是否已授
    public static boolean isPermissionGranted(Activity activity){
        // Android 6.0（API 级别 23）或更高版本的设备上需要运行时授权，而Android 5.1（API 级别 22）或更低版本的设备上，则系统会自动授予相应的权限
        if(Build.VERSION.SDK_INT >= 23){
            for(int i = 0; i < permission.length;i++) {
                int checkPermission = ContextCompat.checkSelfPermission(activity,permission[i]);
                /***
                 * checkPermission返回两个值
                 * 有权限: PackageManager.PERMISSION_GRANTED
                 * 无权限: PackageManager.PERMISSION_DENIED
                 */
                if(checkPermission != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
            return true;
        }else{
            return true;
        }
    }

    public static boolean checkPermission(Activity activity){
        boolean a = isPermissionGranted(activity);
        if(a) {
            return true;
        } else {
            // 如果没有设置过权限许可，则弹出系统的授权窗口
            ActivityCompat.requestPermissions(activity, permission, REQUEST_CODE);
            return false;
        }
    }
}
