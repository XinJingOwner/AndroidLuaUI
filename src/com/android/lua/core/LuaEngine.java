package com.android.lua.core;

import java.io.File;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.content.Context;

import com.android.lua.core.extend.AssetLoaderFunc;
import com.android.lua.core.extend.PrintFunc;

/**
 * Lua engine.
 * @author lizhennian
 * @version 0.0.1 2015-03-05
 */
public class LuaEngine {

    private static final String TAG = "LuaEngine";

    private volatile static LuaEngine sEngine;

    private final LuaState mLuaState;

    private LuaEngine() {
        this.mLuaState = LuaStateFactory.newLuaState();
        this.mLuaState.openLibs();
    }

    public static LuaEngine getInstance() {
        if (sEngine == null) {
            synchronized (LuaEngine.class) {
                if (sEngine == null) {
                    sEngine = new LuaEngine();
                }
            }
        }

        return sEngine;
    }

    public LuaState getLuaState() {
        return this.mLuaState;
    }

    /**
     * Add a path to find lua files in
     * @param path
     *            to be added to the Lua path
     * @throws IllegalArgumentException
     */
    public void addSearchPath(String path) {
        if (path == null || path.length() == 0 || path.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking addSearchPath function.");
        }

        this.mLuaState.getGlobal("package");

        this.mLuaState.getField(-1, "path"); // package path
        String customPath = path + File.separator + "?.lua";
        this.mLuaState.pushString(";" + customPath); // package path custom
        this.mLuaState.concat(2); // package pathCustom
        this.mLuaState.setField(-2, "path"); // package

        this.mLuaState.pop(1);
    }

    /**
     * Add lua loader, now it is used on android.
     * @throws LuaException
     *             IllegalArgumentException
     */
    public void addLuaLoader(JavaFunction func) throws LuaException {
        if (func == null) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking addLuaLoader function.");
        }

        this.mLuaState.getGlobal("package");
        this.mLuaState.getField(-1, "loaders");
        int nLoaders = this.mLuaState.objLen(-1);

        this.mLuaState.pushJavaFunction(func);

        this.mLuaState.rawSetI(-2, nLoaders + 1);
        this.mLuaState.pop(2);
    }

    /**
     * reload script code contained in the given string.
     * @param moduleFileName
     *            String object holding the filename of the script file that is
     *            to be executed
     * @return 0 if the string is excuted correctly.
     *         other if the string is excuted wrongly.
     */
    public int reload(String moduleFileName) {
        return 0;
    }

    /**
     * Execute script code contained in the given string.
     * @param codes
     *            holding the valid script code that should be executed.
     * @return 0 if the string is excuted correctly.
     *         other if the string is excuted wrongly.
     */
    public int executeString(String codes) {
        this.safeEvalLua(codes);
        return 0;
    }

    /**
     * Execute a script file.
     * @param filename
     *            String object holding the filename of the script file that is
     *            to be executed
     */
    public int executeScriptFile(String filename) {
        return 0;
    }

    /**
     * Execute a scripted global function.
     * <b>The function should not take any parameters and should return an
     * integer.
     * @param functionName
     *            String object holding the name of the function, in the global
     *            script environment, that is to be executed.
     * @return The integer value returned from the script function.
     */
    public int executeGlobalFunction(String functionName, Object... args) {
        this.mLuaState.getField(LuaState.LUA_GLOBALSINDEX, functionName);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        return this.mLuaState.pcall(args.length, 0, 0);
    }

    /**
     * Execute a scripted global function.
     * <b>The function should not take any parameters and should return an
     * integer.
     * @param functionName
     *            String object holding the name of the function, in the global
     *            script environment, that is to be executed.
     * @return The integer value returned from the script function.
     */
    public int executeObjectFunction(String objectName, String functionName,
            Object... args) {
        this.mLuaState.getGlobal(objectName);
        this.mLuaState.getField(-1, functionName);

        this.mLuaState.getGlobal(objectName);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        int error = this.mLuaState.pcall(1 + args.length, 0, 0);
        this.mLuaState.pop(1);

        return error;
    }

    public void useExtend(Context context) {
        PrintFunc print = new PrintFunc(this.mLuaState);
        try {
            print.register("print");
        } catch (LuaException e) {
            e.printStackTrace();
        }

        AssetLoaderFunc assetLoader = new AssetLoaderFunc(this.mLuaState,
                context);
        assetLoader.setSubDirectory("luas");
        try {
            this.addLuaLoader(assetLoader);
        } catch (LuaException e) {
            e.printStackTrace();
        }

        this.addSearchPath(context.getFilesDir().getAbsolutePath());

    }

    private String safeEvalLua(String src) {
        String res = null;

        try {
            this.evalLua(src);
        } catch (LuaException e) {
            res = e.getMessage() + "\n";
        }

        return res;
    }

    private void evalLua(String src) throws LuaException {
        this.mLuaState.setTop(0);
        int error = this.mLuaState.LloadString(src);
        if (error == 0) {
            error = this.mLuaState.pcall(0, 0, 0);
            if (error == 0) {
                return;
            }
        }

        throw new LuaException(this.errorReason(error) + ": "
                + this.mLuaState.toString(-1));
    }

    private String printStackTrace() {
        this.mLuaState.getGlobal("debug");
        this.mLuaState.getField(-1, "traceback");
        this.mLuaState.pcall(0, 0, 0);
        String traceback = this.mLuaState.toString(-1);

        this.mLuaState.pop(1);

        return traceback;
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
