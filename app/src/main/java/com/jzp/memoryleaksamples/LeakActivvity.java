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

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Description :
 * Author : jzp
 * Date   : 17/9/7
 */

public class LeakActivvity extends Activity implements View.OnClickListener {

    //static
    static TextView staticView;
    static Activity staticActivity = null;
    static SomeInnerClass someInnerClass;

    String InnerClassParam = "静态类持有外部引用";
    //单例
    SingleDemo single = null;
    //handler
    private final Handler mLeakyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("FRANK", "handle message");
        }
    };
    //thread
    private LeakedThread mThread;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leak);

        findViewById(R.id.leak_singleton).setOnClickListener(this);
        findViewById(R.id.leak_static).setOnClickListener(this);
        findViewById(R.id.leak_static_innerclass).setOnClickListener(this);
        findViewById(R.id.leak_thread).setOnClickListener(this);
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
        Toast.makeText(this,"单例context泄漏",Toast.LENGTH_SHORT).show();
    }

    private void leakStatic() {

        staticView = new TextView(LeakActivvity.this);

        if (staticActivity == null) {
            staticActivity = this;
        }

        Toast.makeText(this,"static泄漏",Toast.LENGTH_SHORT).show();
    }

    private void leakStaticInnerClass() {
        if (someInnerClass == null) {
            someInnerClass = new SomeInnerClass();
        }
        Toast.makeText(this,"内部类调用泄漏",Toast.LENGTH_SHORT).show();

    }

    private void leakThread() {
        //thread
        mThread = new LeakedThread();
        mThread.start();

        //handler
        mLeakyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //持有InnerClassParam控件无法释放
                String test = InnerClassParam;
                Log.e("FRANK", "in run()");
            }
        }, 1000 * 60 * 10);

        //AsyncTask
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (true) {
                    SystemClock.sleep(10000);
                }
            }
        }.execute();

        Toast.makeText(this,"线程调用泄漏",Toast.LENGTH_SHORT).show();

    }


    class SomeInnerClass {
    }


    private class LeakedThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SystemClock.sleep(10000);
            }
        }
    }

}
