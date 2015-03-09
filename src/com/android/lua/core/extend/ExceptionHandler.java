package com.android.lua.core.extend;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

public class ExceptionHandler extends JavaFunction {
    private static final String TAG = "LuaEngine";

    public ExceptionHandler(LuaState luaState) {
        super(luaState);
    }

    @Override
    public int execute() throws LuaException {
        String error = this.mLuaState.toString(-1);

        this.mLuaState.getGlobal("debug");
        this.mLuaState.getField(-1, "traceback");
        this.mLuaState.pcall(0, 0, 0);
        String traceback = this.mLuaState.toString(-1);

        traceback = error + "\t" + traceback;

        this.mLuaState.remove(-2);

        return 0;
    }

}
