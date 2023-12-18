package com.example.tjx.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * @Author l-3177
 * Create by l-3177 in 2019-05-10
 */
public class App extends Application {

    private static Context mContext;//上下文
    private static Thread mMainThread;//主线程
    private static long mMainThreadId;//主线程id
    private static Handler mHandler;//主线程Handler

    @Override
    public void onCreate() {
        super.onCreate();
        //全局属性赋值
        mContext = getApplicationContext();
        mMainThread = Thread.currentThread();
        mMainThreadId = android.os.Process.myTid();
        mHandler = new Handler();
    }

    public static Context getmContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }


    public static long getMainThreadId() {
        return mMainThreadId;
    }

    public static void setMainThreadId(long mMainThreadId) {
        App.mMainThreadId = mMainThreadId;
    }

    public static Handler getMainHandler() {
        return mHandler;
    }

    public static void setMainHandler(Handler mHandler) {
        App.mHandler = mHandler;
    }


}
