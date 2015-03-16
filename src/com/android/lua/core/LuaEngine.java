package com.android.lua.core;

import java.io.File;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.content.Context;
import android.util.Log;

import com.android.lua.core.extend.AssetLoaderFunc;
import com.android.lua.core.extend.PrintFunc;

/**
 * Lua脚本引擎.
 * @author lizhennian
 * @version 0.0.1
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

    /**
     * 返回引擎状态对象.
     * @return 状态对象
     */
    public LuaState getLuaState() {
        return this.mLuaState;
    }

    /**
     * 添加路径到path环境变量中.
     * @param path
     *            搜索路径
     * @throws IllegalArgumentException
     *             当参数为空字符时抛出
     */
    public void addSearchPath(String path) {
        if (this.isBlank(path)) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking addSearchPath function.");
        }

        this.mLuaState.getGlobal("package");

        this.mLuaState.getField(-1, "path");
        String customPath = path + File.separator + "?.lua";
        this.mLuaState.pushString(";" + customPath);
        this.mLuaState.concat(2);
        this.mLuaState.setField(-2, "path");

        this.mLuaState.pop(1);
    }

    /**
     * 添加加载器.
     * <p>
     * 引擎会遍历环境中所有加载器，并尝试加载脚本指定脚本文件.
     * @throws LuaException
     *             添加失败时抛出
     * @throws IllegalArgumentException
     *             当参数为空时抛出
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
     * 执行包含脚本代码的字符串.
     * @param codes
     *            包含脚本代码的字符串
     * @throws LuaException
     *             执行脚本失败时抛出
     */
    public void executeString(String codes) throws LuaException {
        this.evalLua(codes);
    }

    /**
     * 执行脚本文件.
     * @param filename
     *            脚本文件名称
     * @throws LuaException
     *             执行脚本文件失败时抛出
     * @throws IllegalArgumentException
     *             当参数为空字符时抛出
     */
    public void executeScriptFile(String filename) throws LuaException {
        if (this.isBlank(filename)) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking executeScriptFile function.");
        }

        this.mLuaState.setTop(0);
        int error = this.mLuaState.LdoFile(filename);
        if (error != 0) {
            throw this.generateLuaException(error);
        }
    }

    /**
     * 执行一个全局的lua方法.
     * @param functionName
     *            方法名称
     * @param args
     *            方法参数
     * @throws LuaException
     *             执行方法失败时抛出
     * @throws IllegalArgumentException
     *             当参数为空时抛出
     */
    public void executeGlobalFunction(String functionName, Object... args)
            throws LuaException {
        if (this.isBlank(functionName)) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking executeGlobalFunction function.");
        }

        this.mLuaState.getField(LuaState.LUA_GLOBALSINDEX, functionName);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        int error = this.mLuaState.pcall(args.length, 0, 0);

        if (error != 0) {
            throw this.generateLuaException(error);
        }
    }

    /**
     * 执行指定模块下的指定方法.
     * @param moduleName
     *            模块名称
     * @param functionName
     *            方法名称
     * @param args
     *            方法参数
     * @throws LuaException
     *             方法执行失败时抛出
     * @throws IllegalArgumentException
     *             当参数为空字符时抛出
     */
    public void executeModuleFunction(String moduleName, String functionName,
            Object... args) throws LuaException {
        if (this.isBlank(moduleName) || this.isBlank(functionName)) {
            throw new IllegalArgumentException(
                    "Illegal argument when invoking executeModuleFunction function.");
        }

        this.evalLua("require('" + moduleName + "')");

        this.mLuaState.getGlobal(moduleName);
        this.mLuaState.getField(-1, functionName);

        this.mLuaState.getGlobal(moduleName);

        for (Object param : args) {
            this.mLuaState.pushJavaObject(param);
        }

        int error = this.mLuaState.pcall(1 + args.length, 0, 0);

        if (error != 0) {
            throw this.generateLuaException(error);
        }
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
            Log.e(TAG, "[safeEvalLua] " + res);
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

        throw this.generateLuaException(error);
    }

    public String printStackTrace() {
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

    private LuaException generateLuaException(int errorCode) {
        return new LuaException(this.errorReason(errorCode) + ": "
                + this.mLuaState.toString(-1));
    }

    private boolean isBlank(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }
}
