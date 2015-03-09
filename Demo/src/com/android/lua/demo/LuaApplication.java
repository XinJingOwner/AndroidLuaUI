package com.android.lua.demo;

import android.app.Application;

import com.android.lua.core.LuaEngine;

public class LuaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LuaEngine.getInstance().useExtend(this);
    }

}
