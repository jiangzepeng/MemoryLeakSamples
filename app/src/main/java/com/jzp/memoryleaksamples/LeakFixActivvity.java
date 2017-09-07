package com.jzp.memoryleaksamples;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Description : 修复内存的泄漏
 * Author : jzp
 * Date   : 17/9/7
 */

public class LeakFixActivvity extends Activity implements View.OnClickListener {

    // FIXED: remove static keywords
    TextView staticView;
    Activity staticActivity = null;
    SomeInnerClass someInnerClass;

    String InnerClassParam = "静态类持有外部引用";
    //single
    SingleDemo single = null;

    //handler
    private final Handler mLeakyHandler = new MyHandler();
    private final MyRunnable myRunnable = new MyRunnable();

    //thread
    private LeakedThread mThread;

    //Task
    private DoNothingTask doNothingTask = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leak_fix);

        findViewById(R.id.leak_singleton).setOnClickListener(this);
        findViewById(R.id.leak_static).setOnClickListener(this);
        findViewById(R.id.leak_static_innerclass).setOnClickListener(this);
        findViewById(R.id.leak_thread).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // FIXED: unregister it onDestroy
        single.unRegister(this);
        // FIXED: kill the thread in activity onDestroy
        mThread.interrupt();
        // FIXED: remove callback in activity onDestroy
        mLeakyHandler.removeCallbacks(myRunnable);
        // FIXED: should cancel the task in activity onDestroy()
        doNothingTask.cancel(true);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leak_singleton:
                leakSingleton();
                break;
            case R.id.leak_static:
                leakStatic();
                break;
            case R.id.leak_static_innerclass:
                leakStaticInnerClass();
                break;
            case R.id.leak_thread:
                leakThread();
                break;
        }
    }

    private void leakSingleton() {
        single = SingleDemo.getInstance(this);
        Toast.makeText(this,"单例context泄漏------修复",Toast.LENGTH_SHORT).show();

    }

    private void leakStatic() {

        staticView = new TextView(LeakFixActivvity.this);

        if (staticActivity == null) {
            staticActivity = this;
        }
        Toast.makeText(this,"static泄漏------修复",Toast.LENGTH_SHORT).show();

    }

    private void leakStaticInnerClass() {
        if (someInnerClass == null) {
            someInnerClass = new SomeInnerClass();
        }
        Toast.makeText(this,"内部类调用泄漏------修复",Toast.LENGTH_SHORT).show();

    }

    private void leakThread() {
        //thread
        mThread = new LeakedThread();
        mThread.start();

        //handler
        mLeakyHandler.postDelayed(myRunnable, 1000 * 60 * 10);

        //AsyncTask
        doNothingTask = new DoNothingTask();
        doNothingTask.execute();
        Toast.makeText(this,"线程调用泄漏------修复",Toast.LENGTH_SHORT).show();

    }


    class SomeInnerClass {
    }


    // FIXED: make it static. So it does not have referenced to the containing activity class
    private static class LeakedThread extends Thread {
        @Override
        public void run() {
            // FIXED: check interrupted before the next loop
            while (!isInterrupted()) {
                SystemClock.sleep(10000);
            }
        }
    }

    // FIXED: use static class instead of inner class. static class does not have reference to the containing activity
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.e("FRANK", "handle message");
        }
    }

    // FIXED: use static class instead of inner class. static class does not have reference to the containing activity
    private static class MyRunnable implements Runnable {
        @Override
        public void run() {
            Log.e("FRANK", "in run()");
        }
    }


    private static class DoNothingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // FIXED: should check if cancelled before next loop
            while (!isCancelled()) {
                SystemClock.sleep(1000);
            }
            return null;
        }
    }

}