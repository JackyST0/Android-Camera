package com.example.tjx.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tjx.R;

/**
 * @Author l-3177
 * Create by l-3177 in 2019-05-10
 */
public class UIUtils {
    private UIUtils() {
    }

    /**
     * 得到上下文
     *
     * @return
     */
    public static Context getContext() {
        return App.getmContext();
    }

    /**
     * 得到主线程Handler
     *
     * @return
     */
    public static Handler getMainThreadHandler() {
        return App.getMainHandler();
    }

    /**
     * 得到主线程id
     *
     * @return
     */
    public static long getMainThreadId() {
        return App.getMainThreadId();
    }

    /**
     * 成功
     *
     * @param msg
     */
    public static void showToastSuccess(String msg) {
        if (Thread.currentThread().getId() != getMainThreadId()) {
            getMainThreadHandler().post(() -> {
                showToast(msg, true);
            });
        } else {
            showToast(msg, true);
        }
    }

    public static void showToastFail(String msg) {
        if (Thread.currentThread().getId() != getMainThreadId()) {
            getMainThreadHandler().post(() -> {
                showToast(msg, false);
            });
        } else {
            showToast(msg, false);
        }
    }

    /**
     * 创建成功失败样式
     *
     * @param msg
     * @param success
     */
    private static void showToast(String msg, boolean success) {
        int textColor = success ? R.color.colorSuccess : R.color.colorFail;
        int backgroundColor = success ? R.color.colorBackgroundSuccess : R.color.colorBackgroundFail;
        int duration = success ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        new ColoredToast.Maker(getContext())
                .setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300)
                .setColor(textColor, backgroundColor)
                .makeToast(msg, duration)
                .show();
    }

}
