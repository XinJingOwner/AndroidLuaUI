package com.android.lua.core.extend;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import android.content.Context;
import android.content.res.AssetManager;

public class AssetLoaderFunc extends JavaFunction {
    private final Context mContext;

    public AssetLoaderFunc(Context context, LuaState L) {
        super(L);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public int execute() throws LuaException {
        String name = this.mLuaState.toString(-1);

        AssetManager am = this.mContext.getAssets();
        try {
            // String filename = TextUtils
            // .isEmpty(LuaApplication.this.mLuaScriptPath) ? name
            // + ".lua" : LuaApplication.this.mLuaScriptPath
            // + File.separator + name + ".lua";
            String filename = null;
            InputStream is = am.open(filename);
            byte[] bytes = readAll(is);
            this.mLuaState.LloadBuffer(bytes, name);
            return 1;
        } catch (Exception e) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(os));
            this.mLuaState.pushString("Cannot load module " + name + ":\n"
                    + os.toString());
            return 1;
        }
    }

    private static byte[] readAll(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

}
