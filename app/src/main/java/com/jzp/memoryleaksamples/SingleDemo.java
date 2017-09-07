package com.jzp.memoryleaksamples;

import android.content.Context;

/**
 * Description :单例
 * Author : jzp
 * Date   : 17/9/7
 */

public class SingleDemo {
    private static SingleDemo singleDemo;
    private Context context;

    private SingleDemo(Context context) {
        this.context = context;
    }

    public static synchronized SingleDemo getInstance(Context context)
    {
        if(singleDemo==null)
        {
            singleDemo=new SingleDemo(context);
        }
        return singleDemo;
    }

    //释放掉由于引用的context
    public void unRegister(Context context)
    {
        if(this.context==context)
        {
            this.context=null;
        }
    }
}
