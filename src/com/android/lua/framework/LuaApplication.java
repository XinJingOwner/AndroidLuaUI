package com.android.lua.framework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.text.TextUtils;

/**
 * Lua base application.
 * @author lizhennian
 * @version 0.0.1 2015-02-13
 */
public class LuaApplication extends Application {

    private final StringBuilder mOutput = new StringBuilder();
    protected LuaState mLuaState;
    private String mLuaScriptPath;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initLuaEngine();
    }

    private void initLuaEngine() {
        this.mLuaState = LuaStateFactory.newLuaState();
        this.mLuaState.openLibs();

        try {
            this.getMetadata();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            JavaFunction print = new JavaFunction(this.mLuaState) {
                @Override
                public int execute() throws LuaException {
                    for (int i = 2; i <= this.mLuaState.getTop(); i++) {
                        int type = this.mLuaState.type(i);
                        String stype = this.mLuaState.typeName(type);
                        String val = null;
                        if (stype.equals("userdata")) {
                            Object obj = this.mLuaState.toJavaObject(i);
                            if (obj != null) {
                                val = obj.toString();
                            }
                        } else if (stype.equals("boolean")) {
                            val = this.mLuaState.toBoolean(i) ? "true"
                                    : "false";
                        } else {
                            val = this.mLuaState.toString(i);
                        }
                        if (val == null) {
                            val = stype;
                        }
                        LuaApplication.this.mOutput.append(val);
                        LuaApplication.this.mOutput.append("\t");
                    }
                    LuaApplication.this.mOutput.append("\n");
                    return 0;
                }
            };
            print.register("print");

            JavaFunction assetLoader = new JavaFunction(this.mLuaState) {
                @Override
                public int execute() throws LuaException {
                    String name = this.mLuaState.toString(-1);

                    AssetManager am = LuaApplication.this.getAssets();
                    try {
                        String filename = TextUtils
                                .isEmpty(LuaApplication.this.mLuaScriptPath) ? name
                                + ".lua"
                                : LuaApplication.this.mLuaScriptPath
                                        + File.separator + name + ".lua";
                        InputStream is = am.open(filename);
                        byte[] bytes = readAll(is);
                        this.mLuaState.LloadBuffer(bytes, name);
                        return 1;
                    } catch (Exception e) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        e.printStackTrace(new PrintStream(os));
                        this.mLuaState.pushString("Cannot load module " + name
                                + ":\n" + os.toString());
                        return 1;
                    }
                }
            };

            this.mLuaState.getGlobal("package"); // package
            this.mLuaState.getField(-1, "loaders"); // package loaders
            int nLoaders = this.mLuaState.objLen(-1); // package loaders

            this.mLuaState.pushJavaFunction(assetLoader); // package loaders
                                                          // loader
            this.mLuaState.rawSetI(-2, nLoaders + 1); // package loaders
            this.mLuaState.pop(1); // package

            this.mLuaState.getField(-1, "path"); // package path
            String customPath = this.getFilesDir() + "/?.lua";
            this.mLuaState.pushString(";" + customPath); // package path custom
            this.mLuaState.concat(2); // package pathCustom
            this.mLuaState.setField(-2, "path"); // package
            this.mLuaState.pop(1);

        } catch (Exception e) {
            // TODO
        }
    }

    private void getMetadata() throws NameNotFoundException {
        ApplicationInfo info = this.getPackageManager().getApplicationInfo(
                this.getPackageName(), PackageManager.GET_META_DATA);

        if (info.metaData != null) {
            this.mLuaScriptPath = info.metaData.getString("lua_script_path");
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

    public void callLuaMethod(String method, Object... args) {
        this.mLuaState.getField(LuaState.LUA_GLOBALSINDEX, method);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        this.mLuaState.pcall(args.length, 0, 0);
    }

    public void callLuaMethod(String module, String method, Object... args) {
        this.mLuaState.getGlobal(module);
        this.mLuaState.getField(-1, method);

        this.mLuaState.getGlobal(module);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        this.mLuaState.pcall(1 + args.length, 0, 0);
        this.mLuaState.pop(1);
    }

    public String safeEvalLua(String src) {
        String res = null;
        try {
            res = this.evalLua(src);
        } catch (LuaException e) {
            res = e.getMessage() + "\n";
        }
        return res;
    }

    public String evalLua(String src) throws LuaException {
        this.mLuaState.setTop(0);
        int ok = this.mLuaState.LloadString(src);
        if (ok == 0) {
            this.mLuaState.getGlobal("debug");
            this.mLuaState.getField(-1, "traceback");
            this.mLuaState.remove(-2);
            this.mLuaState.insert(-2);
            ok = this.mLuaState.pcall(0, 0, -2);
            if (ok == 0) {
                String res = this.mOutput.toString();
                this.mOutput.setLength(0);
                return res;
            }
        }
        throw new LuaException(this.errorReason(ok) + ": "
                + this.mLuaState.toString(-1));
        // return null;

    }

    private String errorReason(int error) {
        switch (error) {
        case 4:
            return "Out of memory";
        case 3:
            return "Syntax error";
        case 2:
            return "Runtime error";
        case 1:
            return "Yield error";
        }
        return "Unknown error " + error;
    }
}
