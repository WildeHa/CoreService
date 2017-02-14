package com.example.oshao.coreservice;

import android.app.Application;
import android.content.Context;

/**
 * Created by oshao on 1/20/2017.
 */

public class GlobalVirable extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        GlobalVirable.context = context;
    }
}
