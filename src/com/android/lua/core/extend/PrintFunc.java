package com.android.lua.core.extend;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

public class PrintFunc extends JavaFunction {

    public PrintFunc(LuaState L) {
        super(L);
    }

    @Override
    public int execute() throws LuaException {
        StringBuilder logBuilder = new StringBuilder();

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
                val = this.mLuaState.toBoolean(i) ? "true" : "false";
            } else {
                val = this.mLuaState.toString(i);
            }
            if (val == null) {
                val = stype;
            }

            logBuilder.append(val);
        }

        return 0;
    }

}
