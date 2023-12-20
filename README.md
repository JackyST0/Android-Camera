# Android-Camera

## Android：
- ### Handler
![Handler](https://github.com/JackyST0/Java-Technology-Stack/raw/master/%E7%9B%B8%E5%85%B3%E5%9B%BE%E7%89%87/Handler.png)

- ### Activity-Fragment
![Activity-Fragment](https://github.com/JackyST0/Java-Technology-Stack/raw/master/%E7%9B%B8%E5%85%B3%E5%9B%BE%E7%89%87/Activity-Fragment.png)

- ### 切换到主线程更新UI的几种方法
    - 方法一：view.post(Runnable action)
        ```
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("更新textView");
                //还可以更新其他的控件
                imageView.setBackgroundResource(R.drawable.update);
            }
        });
        ```
    - 方法二： activity.runOnUiThread(Runnable action)
        ```
        public void updateUI(final Context context) {
            ((MainActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //此时已在主线程中，可以更新UI了
                }
            });
        }
        ```
    - 方法三： Handler机制
        - Handler.post
            ```
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    //已在主线程中，可以更新UI
                }
            });
            ```
        - Handler.sendMessage(msg)
            ```
            Handler myHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case 0:
                            //更新UI等
                            break;
                        case 1:
                            //更新UI等
                            break;
                        default:
                            break;
                    }
                }
            }
            ```
            ```
            /**
            * 获取消息，尽量用obtainMessage()方法，查看源码发现，该方法节省内存。
            * 不提倡用Messenger msg=new Messenger()这种方法，每次都去创建一个对象，肯定不节省内存啦！
            * 至于为什么该方法还存在，估计还是有存在的必要吧。（留作以后深入研究）
            */
            Message msg = myHandler.obtainMessage();
            msg.what = 0; //消息标识
            myHandler.sendMessage(msg); //发送消息
            ```
    - 方法四： 使用AsyncTask
        ```
        /**
        * 该类中方法的执行顺序依次为：onPreExecute, doInBackground, onPostExecute
        */
            private class MyAsyncTask extends AsyncTask<String, Integer, String> {
                /**
                * 主线程中执行
                * 在execute()被调用后首先执行
                * 一般用来在执行后台任务前对UI做一些标记
                */
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    System.out.println("MyAsyncTask.onPreExecute");
                }

                /**
                * 子线程中执行，执行一些耗时操作，关键方法
                * 在执行过程中可以调用publishProgress(Progress... values)来更新进度信息。
                */
                @Override
                protected String doInBackground(String... params) {
                    System.out.println("MyAsyncTask.doInBackground");
                    //只是模拟了耗时操作
                    int count = 0;
                    for (int i = 0; i < 10; i++) {
                        try {
                            count++;
                            publishProgress((count % 100) * 10);
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // publishProgress((int) ((count / (float) total) * 100));
                    return "耗时操作执行完毕";
                }

                /**
                * 在主线程中，当后台操作结束时，此方法将会被调用
                * 计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。
                */
                @Override
                protected void onPostExecute(String aVoid) {
                    super.onPostExecute(aVoid);
                    System.out.println("MyAsyncTask.onPostExecute aVoid=" + aVoid);
                    textView.setText(aVoid);
                }
            }
        ```

- ### 四大组件
    - activity:
        - 负责展示用户界面，是用户直接交互的对象。
        - 一个应用可以有多个 activity，对应不同的界面。
        - 每个 activity 都对应着生命周期，如 onCreate、onStart、onResume 等。
        - 负责处理用户输入、显示数据、执行与界面相关的逻辑。
    - service:
        - 在后台运行，不提供直接的界面。
        - 用于执行长期运行的任务，不会因为 activity 的销毁而中断。
        - 可以用于播放音乐、下载文件、网络通信等后台操作。
        - 没有生命周期，需要手动启动和停止。
    - content provider:
        - 是一种数据共享机制，提供对数据的统一访问接口。
        - 其他应用可以通过 content provider 访问和修改另一个应用的数据。
        - 通常用于共享应用之间难以直接访问的数据，如联系人、日历等。
        - 需要实现 ContentProvider 类及其方法，定义数据访问逻辑。
    - broadcast receiver:
        - 接收系统或其他应用发送的广播消息。
        - 用于响应系统事件或其他应用发出的通知。
        - 不提供界面，只作为响应广播消息的代码段。
        - 需要注册和反注册 broadcast receiver，指定要监听的广播动作。
    - 总结:
        - activity: 展示界面，处理用户交互。
        - service: 执行后台任务，不会因 activity 销毁而中断。
        - content provider: 分享数据，其他应用可通过它访问数据。
        - broadcast receiver: 接收系统或其他应用发送的广播消息，并作出响应


## Camera:
- ### Camera1:
    - Android API 中最早用于控制相机拍摄的接口，已经过时，并不推荐使用。
    - 使用起来相对复杂，需要处理很多底层细节，错误处理和兼容性方面也面临挑战。
    - 仍然存在于系统中，但官方并不鼓励新项目使用，只用于维护旧项目兼容性。

- ### Camera2:
    - Camera1 的替代方案，提供更灵活和强大的相机控制功能。
    - 包括对预览流、捕获图像和视频格式等方面的增强控制。
    - 使用起来比 Camera1 更加复杂，需要编写更多代码。
    - 目前是推荐用于控制相机拍摄的主要接口。

- ### CameraX:
    - 基于 Camera2 API 封装的 Jetpack 库，简化了相机开发流程。
    - 提供生命周期管理、自动配置和抽象化 API，降低开发难度。
    - 与 Camera2 相比，功能相对有限，但不失常用功能。
    - 是最新推荐用于相机开发的接口，尤其适合新手开发者。

- ### Intent:
    - Android 系统中用于启动 Activity 和传递数据的机制。
    - 可以通过 Intent 指定要启动的 Activity 和携带相关数据。
    - 用于启动相机拍摄操作时也可以使用 Intent，比如可以传递额外的配置参数。

- ### 总结:
    - Camera1 过时，不推荐使用。
    - Camera2 功能强大，但复杂。
    - CameraX 轻量易用，推荐新手使用。
    - Intent 用于启动 Activity 和传递数据，与相机开发结合使用。