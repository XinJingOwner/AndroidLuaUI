/*
 * $Id: LuaState.java,v 1.9 2006/12/22 14:06:40 thiago Exp $
 * Copyright (C) 2003-2007 Kepler Project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.keplerproject.luajava;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

/**
 * LuaState if the main class of LuaJava for the Java developer.
 * LuaState is a mapping of most of Lua's C API functions.
 * LuaState also provides many other functions that will be used to manipulate
 * objects between Lua and Java.
 * @author Thiago Ponte
 */
public class LuaState {
    private final static String LUAJAVA_LIB = "luajava";

    final public static int LUA_GLOBALSINDEX = -10002;
    final public static int LUA_REGISTRYINDEX = -10000;

    final public static int LUA_TNONE = -1;
    final public static int LUA_TNIL = 0;
    final public static int LUA_TBOOLEAN = 1;
    final public static int LUA_TLIGHTUSERDATA = 2;
    final public static int LUA_TNUMBER = 3;
    final public static int LUA_TSTRING = 4;
    final public static int LUA_TTABLE = 5;
    final public static int LUA_TFUNCTION = 6;
    final public static int LUA_TUSERDATA = 7;
    final public static int LUA_TTHREAD = 8;

    /**
     * Specifies that an unspecified (multiple) number of return arguments
     * will be returned by a call.
     */
    final public static int LUA_MULTRET = -1;

    /*
     * error codes for `lua_load' and `lua_pcall'
     */
    /**
     * a runtime error.
     */
    final public static int LUA_ERRRUN = 1;

    /**
   * 
   */
    final public static int LUA_YIELD = 2;

    /**
     * syntax error during pre-compilation.
     */
    final public static int LUA_ERRSYNTAX = 3;

    /**
     * memory allocation error. For such errors, Lua does not call
     * the error handler function.
     */
    final public static int LUA_ERRMEM = 4;

    /**
     * error while running the error handler function.
     */
    final public static int LUA_ERRERR = 5;

    /**
     * Opens the library containing the luajava API
     */
    static {
        System.loadLibrary(LUAJAVA_LIB);
    }

    private CPtr luaState;

    private final int stateId;

    private List<String> packages;

    /**
     * Constructor to instance a new LuaState and initialize it with LuaJava's
     * functions
     * @param stateId
     */
    protected LuaState(int stateId) {
        this.luaState = this._open();
        this.luajava_open(this.luaState, stateId);
        this.stateId = stateId;
        this.packages = new ArrayList<String>();
    }

    /**
     * Receives a existing state and initializes it
     * @param luaState
     */
    protected LuaState(CPtr luaState) {
        this.luaState = luaState;
        this.stateId = LuaStateFactory.insertLuaState(this);
        this.luajava_open(luaState, this.stateId);
    }

    /**
     * Closes state and removes the object from the LuaStateFactory
     */
    public synchronized void close() {
        LuaStateFactory.removeLuaState(this.stateId);
        this._close(this.luaState);
        this.luaState = null;
    }

    /**
     * Returns <code>true</code> if state is closed.
     */
    public synchronized boolean isClosed() {
        return this.luaState == null;
    }

    /**
     * Return the long representing the LuaState pointer
     * @return long
     */
    public long getCPtrPeer() {
        return (this.luaState != null) ? this.luaState.getPeer() : 0;
    }

    /**
     * Return imported package name list.
     * @return
     */
    public List<String> getPackages() {
        return this.packages;
    }

    public boolean addPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        for (String item : this.packages) {
            if (packageName.equals(item)) {
                return true;
            }
        }

        this.packages.add(packageName);

        return true;
    }

    /********************* Lua Native Interface *************************/

    private synchronized native CPtr _open();

    private synchronized native void _close(CPtr ptr);

    private synchronized native CPtr _newthread(CPtr ptr);

    // Stack manipulation
    private synchronized native int _getTop(CPtr ptr);

    private synchronized native void _setTop(CPtr ptr, int idx);

    private synchronized native void _pushValue(CPtr ptr, int idx);

    private synchronized native void _remove(CPtr ptr, int idx);

    private synchronized native void _insert(CPtr ptr, int idx);

    private synchronized native void _replace(CPtr ptr, int idx);

    private synchronized native int _checkStack(CPtr ptr, int sz);

    private synchronized native void _xmove(CPtr from, CPtr to, int n);

    // Access functions
    private synchronized native int _isNumber(CPtr ptr, int idx);

    private synchronized native int _isString(CPtr ptr, int idx);

    private synchronized native int _isCFunction(CPtr ptr, int idx);

    private synchronized native int _isUserdata(CPtr ptr, int idx);

    private synchronized native int _type(CPtr ptr, int idx);

    private synchronized native String _typeName(CPtr ptr, int tp);

    private synchronized native int _equal(CPtr ptr, int idx1, int idx2);

    private synchronized native int _rawequal(CPtr ptr, int idx1, int idx2);

    private synchronized native int _lessthan(CPtr ptr, int idx1, int idx2);

    private synchronized native double _toNumber(CPtr ptr, int idx);

    private synchronized native int _toInteger(CPtr ptr, int idx);

    private synchronized native int _toBoolean(CPtr ptr, int idx);

    private synchronized native String _toString(CPtr ptr, int idx);

    private synchronized native int _objlen(CPtr ptr, int idx);

    private synchronized native CPtr _toThread(CPtr ptr, int idx);

    // Push functions
    private synchronized native void _pushNil(CPtr ptr);

    private synchronized native void _pushNumber(CPtr ptr, double number);

    private synchronized native void _pushInteger(CPtr ptr, int integer);

    private synchronized native void _pushString(CPtr ptr, String str);

    private synchronized native void _pushString(CPtr ptr, byte[] bytes, int n);

    private synchronized native void _pushBoolean(CPtr ptr, int bool);

    // Get functions
    private synchronized native void _getTable(CPtr ptr, int idx);

    private synchronized native void _getField(CPtr ptr, int idx, String k);

    private synchronized native void _rawGet(CPtr ptr, int idx);

    private synchronized native void _rawGetI(CPtr ptr, int idx, int n);

    private synchronized native void _createTable(CPtr ptr, int narr, int nrec);

    private synchronized native int _getMetaTable(CPtr ptr, int idx);

    private synchronized native void _getFEnv(CPtr ptr, int idx);

    // Set functions
    private synchronized native void _setTable(CPtr ptr, int idx);

    private synchronized native void _setField(CPtr ptr, int idx, String k);

    private synchronized native void _rawSet(CPtr ptr, int idx);

    private synchronized native void _rawSetI(CPtr ptr, int idx, int n);

    private synchronized native int _setMetaTable(CPtr ptr, int idx);

    private synchronized native int _setFEnv(CPtr ptr, int idx);

    private synchronized native void _call(CPtr ptr, int nArgs, int nResults);

    private synchronized native int _pcall(CPtr ptr, int nArgs, int Results,
            int errFunc);

    // Coroutine Functions
    private synchronized native int _yield(CPtr ptr, int nResults);

    private synchronized native int _resume(CPtr ptr, int nargs);

    private synchronized native int _status(CPtr ptr);

    // Gargabe Collection Functions
    final public static int LUA_GCSTOP = 0;
    final public static int LUA_GCRESTART = 1;
    final public static int LUA_GCCOLLECT = 2;
    final public static int LUA_GCCOUNT = 3;
    final public static int LUA_GCCOUNTB = 4;
    final public static int LUA_GCSTEP = 5;
    final public static int LUA_GCSETPAUSE = 6;
    final public static int LUA_GCSETSTEPMUL = 7;

    private synchronized native int _gc(CPtr ptr, int what, int data);

    // Miscellaneous Functions
    private synchronized native int _error(CPtr ptr);

    private synchronized native int _next(CPtr ptr, int idx);

    private synchronized native void _concat(CPtr ptr, int n);

    // Some macros
    private synchronized native void _pop(CPtr ptr, int n);

    private synchronized native void _newTable(CPtr ptr);

    private synchronized native int _strlen(CPtr ptr, int idx);

    private synchronized native int _isFunction(CPtr ptr, int idx);

    private synchronized native int _isTable(CPtr ptr, int idx);

    private synchronized native int _isNil(CPtr ptr, int idx);

    private synchronized native int _isBoolean(CPtr ptr, int idx);

    private synchronized native int _isThread(CPtr ptr, int idx);

    private synchronized native int _isNone(CPtr ptr, int idx);

    private synchronized native int _isNoneOrNil(CPtr ptr, int idx);

    private synchronized native void _setGlobal(CPtr ptr, String name);

    private synchronized native void _getGlobal(CPtr ptr, String name);

    private synchronized native int _getGcCount(CPtr ptr);

    // LuaLibAux
    private synchronized native int _LdoFile(CPtr ptr, String fileName);

    private synchronized native int _LdoString(CPtr ptr, String string);

    // private synchronized native int _doBuffer(CPtr ptr, byte[] buff, long sz,
    // String n);

    private synchronized native int _LgetMetaField(CPtr ptr, int obj, String e);

    private synchronized native int _LcallMeta(CPtr ptr, int obj, String e);

    private synchronized native int _Ltyperror(CPtr ptr, int nArg, String tName);

    private synchronized native int _LargError(CPtr ptr, int numArg,
            String extraMsg);

    private synchronized native String _LcheckString(CPtr ptr, int numArg);

    private synchronized native String _LoptString(CPtr ptr, int numArg,
            String def);

    private synchronized native double _LcheckNumber(CPtr ptr, int numArg);

    private synchronized native double _LoptNumber(CPtr ptr, int numArg,
            double def);

    private synchronized native int _LcheckInteger(CPtr ptr, int numArg);

    private synchronized native int _LoptInteger(CPtr ptr, int numArg, int def);

    private synchronized native void _LcheckStack(CPtr ptr, int sz, String msg);

    private synchronized native void _LcheckType(CPtr ptr, int nArg, int t);

    private synchronized native void _LcheckAny(CPtr ptr, int nArg);

    private synchronized native int _LnewMetatable(CPtr ptr, String tName);

    private synchronized native void _LgetMetatable(CPtr ptr, String tName);

    private synchronized native void _Lwhere(CPtr ptr, int lvl);

    private synchronized native int _Lref(CPtr ptr, int t);

    private synchronized native void _LunRef(CPtr ptr, int t, int ref);

    private synchronized native int _LgetN(CPtr ptr, int t);

    private synchronized native void _LsetN(CPtr ptr, int t, int n);

    private synchronized native int _LloadFile(CPtr ptr, String fileName);

    private synchronized native int _LloadBuffer(CPtr ptr, byte[] buff,
            long sz, String name);

    private synchronized native int _LloadString(CPtr ptr, String s);

    private synchronized native String _Lgsub(CPtr ptr, String s, String p,
            String r);

    private synchronized native String _LfindTable(CPtr ptr, int idx,
            String fname, int szhint);

    private synchronized native void _openBase(CPtr ptr);

    private synchronized native void _openTable(CPtr ptr);

    private synchronized native void _openIo(CPtr ptr);

    private synchronized native void _openOs(CPtr ptr);

    private synchronized native void _openString(CPtr ptr);

    private synchronized native void _openMath(CPtr ptr);

    private synchronized native void _openDebug(CPtr ptr);

    private synchronized native void _openPackage(CPtr ptr);

    private synchronized native void _openLibs(CPtr ptr);

    // Java Interface -----------------------------------------------------

    public LuaState newThread() {
        LuaState l = new LuaState(this._newthread(this.luaState));
        LuaStateFactory.insertLuaState(l);
        return l;
    }

    // STACK MANIPULATION

    public int getTop() {
        return this._getTop(this.luaState);
    }

    public void setTop(int idx) {
        this._setTop(this.luaState, idx);
    }

    public void pushValue(int idx) {
        this._pushValue(this.luaState, idx);
    }

    public void remove(int idx) {
        this._remove(this.luaState, idx);
    }

    public void insert(int idx) {
        this._insert(this.luaState, idx);
    }

    public void replace(int idx) {
        this._replace(this.luaState, idx);
    }

    public int checkStack(int sz) {
        return this._checkStack(this.luaState, sz);
    }

    public void xmove(LuaState to, int n) {
        this._xmove(this.luaState, to.luaState, n);
    }

    // ACCESS FUNCTION

    public boolean isNumber(int idx) {
        return (this._isNumber(this.luaState, idx) != 0);
    }

    public boolean isString(int idx) {
        return (this._isString(this.luaState, idx) != 0);
    }

    public boolean isFunction(int idx) {
        return (this._isFunction(this.luaState, idx) != 0);
    }

    public boolean isCFunction(int idx) {
        return (this._isCFunction(this.luaState, idx) != 0);
    }

    public boolean isUserdata(int idx) {
        return (this._isUserdata(this.luaState, idx) != 0);
    }

    public boolean isTable(int idx) {
        return (this._isTable(this.luaState, idx) != 0);
    }

    public boolean isBoolean(int idx) {
        return (this._isBoolean(this.luaState, idx) != 0);
    }

    public boolean isNil(int idx) {
        return (this._isNil(this.luaState, idx) != 0);
    }

    public boolean isThread(int idx) {
        return (this._isThread(this.luaState, idx) != 0);
    }

    public boolean isNone(int idx) {
        return (this._isNone(this.luaState, idx) != 0);
    }

    public boolean isNoneOrNil(int idx) {
        return (this._isNoneOrNil(this.luaState, idx) != 0);
    }

    public int type(int idx) {
        return this._type(this.luaState, idx);
    }

    public String typeName(int tp) {
        return this._typeName(this.luaState, tp);
    }

    public int equal(int idx1, int idx2) {
        return this._equal(this.luaState, idx1, idx2);
    }

    public int rawequal(int idx1, int idx2) {
        return this._rawequal(this.luaState, idx1, idx2);
    }

    public int lessthan(int idx1, int idx2) {
        return this._lessthan(this.luaState, idx1, idx2);
    }

    public double toNumber(int idx) {
        return this._toNumber(this.luaState, idx);
    }

    public int toInteger(int idx) {
        return this._toInteger(this.luaState, idx);
    }

    public boolean toBoolean(int idx) {
        return (this._toBoolean(this.luaState, idx) != 0);
    }

    public String toString(int idx) {
        return this._toString(this.luaState, idx);
    }

    public int strLen(int idx) {
        return this._strlen(this.luaState, idx);
    }

    public int objLen(int idx) {
        return this._objlen(this.luaState, idx);
    }

    public LuaState toThread(int idx) {
        return new LuaState(this._toThread(this.luaState, idx));
    }

    // PUSH FUNCTIONS

    public void pushNil() {
        this._pushNil(this.luaState);
    }

    public void pushNumber(double db) {
        this._pushNumber(this.luaState, db);
    }

    public void pushInteger(int integer) {
        this._pushInteger(this.luaState, integer);
    }

    public void pushString(String str) {
        if (str == null) {
            this._pushNil(this.luaState);
        } else {
            this._pushString(this.luaState, str);
        }
    }

    public void pushString(byte[] bytes) {
        if (bytes == null) {
            this._pushNil(this.luaState);
        } else {
            this._pushString(this.luaState, bytes, bytes.length);
        }
    }

    public void pushBoolean(boolean bool) {
        this._pushBoolean(this.luaState, bool ? 1 : 0);
    }

    // GET FUNCTIONS

    public void getTable(int idx) {
        this._getTable(this.luaState, idx);
    }

    /**
    <span class="apii">[-0, +1, <em>e</em>]</span>
    <pre>int lua_getfield (lua_State *L, int index, const char *k);</pre>
    <p>
    Pushes onto the stack the value <code>t[k]</code>,
    where <code>t</code> is the value at the given index.
    As in Lua, this function may trigger a metamethod
    for the "index" event (see <a href="#2.4">&sect;2.4</a>).
    <p>
    Returns the type of the pushed value.
     */
    public void getField(int idx, String k) {
        this._getField(this.luaState, idx, k);
    }

    public void rawGet(int idx) {
        this._rawGet(this.luaState, idx);
    }

    public void rawGetI(int idx, int n) {
        this._rawGetI(this.luaState, idx, n);
    }

    public void createTable(int narr, int nrec) {
        this._createTable(this.luaState, narr, nrec);
    }

    public void newTable() {
        this._newTable(this.luaState);
    }

    // if returns 0, there is no metatable
    public int getMetaTable(int idx) {
        return this._getMetaTable(this.luaState, idx);
    }

    public void getFEnv(int idx) {
        this._getFEnv(this.luaState, idx);
    }

    // SET FUNCTIONS

    public void setTable(int idx) {
        this._setTable(this.luaState, idx);
    }

    public void setField(int idx, String k) {
        this._setField(this.luaState, idx, k);
    }

    public void rawSet(int idx) {
        this._rawSet(this.luaState, idx);
    }

    /**
    <pre>void lua_rawseti (lua_State *L, int index, lua_Integer i);</pre>
    <p>
    Does the equivalent of <code>t[i] = v</code>,
    where <code>t</code> is the table at the given index
    and <code>v</code> is the value at the top of the stack.
    <p>
    This function pops the value from the stack.
    The assignment is raw;
    that is, it does not invoke metamethods.
     */
    public void rawSetI(int idx, int n) {
        this._rawSetI(this.luaState, idx, n);
    }

    // if returns 0, cannot set the metatable to the given object
    public int setMetaTable(int idx) {
        return this._setMetaTable(this.luaState, idx);
    }

    // if object is not a function returns 0
    public int setFEnv(int idx) {
        return this._setFEnv(this.luaState, idx);
    }

    public void call(int nArgs, int nResults) {
        this._call(this.luaState, nArgs, nResults);
    }

    /**
    <pre>int lua_pcall (lua_State *L, int nargs, int nresults, int msgh);</pre>
    <p>
    Calls a function in protected mode.
    <p>
    Both <code>nargs</code> and <code>nresults</code> have the same meaning as
    in <a href="#lua_call"><code>lua_call</code></a>.
    If there are no errors during the call,
    <a href="#lua_pcall"><code>lua_pcall</code></a> behaves exactly like <a href="#lua_call"><code>lua_call</code></a>.
    However, if there is any error,
    <a href="#lua_pcall"><code>lua_pcall</code></a> catches it,
    pushes a single value on the stack (the error message),
    and returns an error code.
    Like <a href="#lua_call"><code>lua_call</code></a>,
    <a href="#lua_pcall"><code>lua_pcall</code></a> always removes the function
    and its arguments from the stack.
    <p>
    If <code>msgh</code> is 0,
    then the error message returned on the stack
    is exactly the original error message.
    Otherwise, <code>msgh</code> is the stack index of a
    <em>message handler</em>.
    (In the current implementation, this index cannot be a pseudo-index.)
    In case of runtime errors,
    this function will be called with the error message
    and its return value will be the message
    returned on the stack by <a href="#lua_pcall"><code>lua_pcall</code></a>.
    <p>
    Typically, the message handler is used to add more debug
    information to the error message, such as a stack traceback.
    Such information cannot be gathered after the return of <a href="#lua_pcall"><code>lua_pcall</code></a>,
    since by then the stack has unwound.
    <p>
    The <a href="#lua_pcall"><code>lua_pcall</code></a> function returns one of the following constants
    (defined in <code>lua.h</code>):
    <ul>
    <li><b><a name="pdf-LUA_OK"><code>LUA_OK</code></a> (0): </b>
    success.</li>
    <li><b><a name="pdf-LUA_ERRRUN"><code>LUA_ERRRUN</code></a>: </b>
    a runtime error.
    </li>
    <li><b><a name="pdf-LUA_ERRMEM"><code>LUA_ERRMEM</code></a>: </b>
    memory allocation error.
    For such errors, Lua does not call the message handler.
    </li>
    <li><b><a name="pdf-LUA_ERRERR"><code>LUA_ERRERR</code></a>: </b>
    error while running the message handler.
    </li>
    <li><b><a name="pdf-LUA_ERRGCMM"><code>LUA_ERRGCMM</code></a>: </b>
    error while running a <code>__gc</code> metamethod.
    (This error typically has no relation with the function being called.)
    </li>
    </ul>
     */
    // returns 0 if ok of one of the error codes defined
    public int pcall(int nArgs, int nResults, int errFunc) {
        return this._pcall(this.luaState, nArgs, nResults, errFunc);
    }

    public int yield(int nResults) {
        return this._yield(this.luaState, nResults);
    }

    public int resume(int nArgs) {
        return this._resume(this.luaState, nArgs);
    }

    public int status() {
        return this._status(this.luaState);
    }

    public int gc(int what, int data) {
        return this._gc(this.luaState, what, data);
    }

    public int getGcCount() {
        return this._getGcCount(this.luaState);
    }

    public int next(int idx) {
        return this._next(this.luaState, idx);
    }

    public int error() {
        return this._error(this.luaState);
    }

    public void concat(int n) {
        this._concat(this.luaState, n);
    }

    // FUNCTION FROM lauxlib
    // returns 0 if ok
    public int LdoFile(String fileName) {
        return this._LdoFile(this.luaState, fileName);
    }

    // returns 0 if ok
    public int LdoString(String str) {
        return this._LdoString(this.luaState, str);
    }

    public int LgetMetaField(int obj, String e) {
        return this._LgetMetaField(this.luaState, obj, e);
    }

    public int LcallMeta(int obj, String e) {
        return this._LcallMeta(this.luaState, obj, e);
    }

    public int Ltyperror(int nArg, String tName) {
        return this._Ltyperror(this.luaState, nArg, tName);
    }

    public int LargError(int numArg, String extraMsg) {
        return this._LargError(this.luaState, numArg, extraMsg);
    }

    public String LcheckString(int numArg) {
        return this._LcheckString(this.luaState, numArg);
    }

    public String LoptString(int numArg, String def) {
        return this._LoptString(this.luaState, numArg, def);
    }

    public double LcheckNumber(int numArg) {
        return this._LcheckNumber(this.luaState, numArg);
    }

    public double LoptNumber(int numArg, double def) {
        return this._LoptNumber(this.luaState, numArg, def);
    }

    public int LcheckInteger(int numArg) {
        return this._LcheckInteger(this.luaState, numArg);
    }

    public int LoptInteger(int numArg, int def) {
        return this._LoptInteger(this.luaState, numArg, def);
    }

    public void LcheckStack(int sz, String msg) {
        this._LcheckStack(this.luaState, sz, msg);
    }

    public void LcheckType(int nArg, int t) {
        this._LcheckType(this.luaState, nArg, t);
    }

    public void LcheckAny(int nArg) {
        this._LcheckAny(this.luaState, nArg);
    }

    public int LnewMetatable(String tName) {
        return this._LnewMetatable(this.luaState, tName);
    }

    public void LgetMetatable(String tName) {
        this._LgetMetatable(this.luaState, tName);
    }

    public void Lwhere(int lvl) {
        this._Lwhere(this.luaState, lvl);
    }

    public int Lref(int t) {
        return this._Lref(this.luaState, t);
    }

    public void LunRef(int t, int ref) {
        this._LunRef(this.luaState, t, ref);
    }

    public int LgetN(int t) {
        return this._LgetN(this.luaState, t);
    }

    public void LsetN(int t, int n) {
        this._LsetN(this.luaState, t, n);
    }

    public int LloadFile(String fileName) {
        return this._LloadFile(this.luaState, fileName);
    }

    public int LloadString(String s) {
        return this._LloadString(this.luaState, s);
    }

    public int LloadBuffer(byte[] buff, String name) {
        return this._LloadBuffer(this.luaState, buff, buff.length, name);
    }

    public String Lgsub(String s, String p, String r) {
        return this._Lgsub(this.luaState, s, p, r);
    }

    public String LfindTable(int idx, String fname, int szhint) {
        return this._LfindTable(this.luaState, idx, fname, szhint);
    }

    // IMPLEMENTED C MACROS

    /**
     <span class="apii">[-n, +0, &ndash;]</span>
     <pre>void lua_pop (lua_State *L, int n);</pre>
     <p>
     Pops <code>n</code> elements from the stack.
     */
    public void pop(int n) {
        // setTop(- (n) - 1);
        this._pop(this.luaState, n);
    }

    /**
     <span class="apii">[-0, +1, <em>e</em>]</span>
     <pre>int lua_getglobal (lua_State *L, const char *name);</pre>
     <p>
     Pushes onto the stack the value of the global <code>name</code>.
     Returns the type of that value.
     * @param global
     */
    public synchronized void getGlobal(String global) {
        // pushString(global);
        // getTable(LUA_GLOBALSINDEX.intValue());
        this._getGlobal(this.luaState, global);
    }

    /**
    <pre>void lua_setglobal (lua_State *L, const char *name);</pre>
    <p>
    Pops a value from the stack and
    sets it as the new value of global <code>name</code>.
    */
    public synchronized void setGlobal(String name) {
        // pushString(name);
        // insert(-2);
        // setTable(LUA_GLOBALSINDEX.intValue());
        this._setGlobal(this.luaState, name);
    }

    // Functions to open lua libraries
    public void openBase() {
        this._openBase(this.luaState);
    }

    public void openTable() {
        this._openTable(this.luaState);
    }

    public void openIo() {
        this._openIo(this.luaState);
    }

    public void openOs() {
        this._openOs(this.luaState);
    }

    public void openString() {
        this._openString(this.luaState);
    }

    public void openMath() {
        this._openMath(this.luaState);
    }

    public void openDebug() {
        this._openDebug(this.luaState);
    }

    public void openPackage() {
        this._openPackage(this.luaState);
    }

    public void openLibs() {
        this._openLibs(this.luaState);
    }

    /********************** Luajava API Library **********************/

    /**
     * Initializes lua State to be used by luajava
     * @param cptr
     * @param stateId
     */
    private synchronized native void luajava_open(CPtr cptr, int stateId);

    /**
     * Gets a Object from a userdata
     * @param L
     * @param idx
     *            index of the lua stack
     * @return Object
     */
    private synchronized native Object _getObjectFromUserdata(CPtr L, int idx)
            throws LuaException;

    /**
     * Returns whether a userdata contains a Java Object
     * @param L
     * @param idx
     *            index of the lua stack
     * @return boolean
     */
    private synchronized native boolean _isObject(CPtr L, int idx);

    /**
     * Pushes a Java Object into the state stack
     * @param L
     * @param obj
     */
    private synchronized native void _pushJavaObject(CPtr L, Object obj);

    /**
     * Pushes a class Object into the state stack
     * @param L
     * @param obj
     */
    private synchronized native void _pushJavaClass(CPtr L, Object obj);

    /**
     * Pushes a JavaFunction into the state stack
     * @param L
     * @param func
     */
    private synchronized native void _pushJavaFunction(CPtr L, JavaFunction func)
            throws LuaException;

    /**
     * Returns whether a userdata contains a Java Function
     * @param L
     * @param idx
     *            index of the lua stack
     * @return boolean
     */
    private synchronized native boolean _isJavaFunction(CPtr L, int idx);

    /**
     * Gets a Object from Lua
     * @param idx
     *            index of the lua stack
     * @return Object
     * @throws LuaException
     *             if the lua object does not represent a java object.
     */
    public Object getObjectFromUserdata(int idx) throws LuaException {
        return this._getObjectFromUserdata(this.luaState, idx);
    }

    /**
     * Tells whether a lua index contains a java Object
     * @param idx
     *            index of the lua stack
     * @return boolean
     */
    public boolean isObject(int idx) {
        return this._isObject(this.luaState, idx);
    }

    /**
     * Pushes a Java Object into the lua stack.<br>
     * This function does not check if the object is from a class that could
     * be represented by a lua type. Eg: java.lang.String could be a lua string.
     * @param obj
     *            Object to be pushed into lua
     */
    public void pushJavaObject(Object obj) {
        this._pushJavaObject(this.luaState, obj);
    }

    /**
     * Pushes a Java class Object into the lua stack.<br>
     * @param obj
     *            Object to be pushed into lua
     */
    public void pushJavaClass(Object obj) {
        this._pushJavaClass(this.luaState, obj);
    }

    /**
     * Pushes a JavaFunction into the state stack
     * @param func
     */
    public void pushJavaFunction(JavaFunction func) throws LuaException {
        this._pushJavaFunction(this.luaState, func);
    }

    /**
     * Returns whether a userdata contains a Java Function
     * @param idx
     *            index of the lua stack
     * @return boolean
     */
    public boolean isJavaFunction(int idx) {
        return this._isJavaFunction(this.luaState, idx);
    }

    /**
     * Pushes into the stack any object value.<br>
     * This function checks if the object could be pushed as a lua type, if not
     * pushes the java object.
     * @param obj
     */
    public void pushObjectValue(Object obj) throws LuaException {
        if (obj == null) {
            this.pushNil();
        } else if (obj instanceof Boolean) {
            Boolean bool = (Boolean) obj;
            this.pushBoolean(bool.booleanValue());
        } else if (obj instanceof Number) {
            this.pushNumber(((Number) obj).doubleValue());
        } else if (obj instanceof String) {
            this.pushString((String) obj);
        } else if (obj instanceof JavaFunction) {
            JavaFunction func = (JavaFunction) obj;
            this.pushJavaFunction(func);
        } else if (obj instanceof LuaObject) {
            LuaObject ref = (LuaObject) obj;
            ref.push();
        } else if (obj instanceof byte[]) {
            this.pushString((byte[]) obj);
        } else {
            this.pushJavaObject(obj);
        }
    }

    /**
     * Function that returns a Java Object equivalent to the one in the given
     * position of the Lua Stack.
     * @param idx
     *            Index in the Lua Stack
     * @return Java object equivalent to the Lua one
     */
    public synchronized Object toJavaObject(int idx) throws LuaException {
        Object obj = null;

        if (this.isBoolean(idx)) {
            obj = new Boolean(this.toBoolean(idx));
        } else if (this.type(idx) == LuaState.LUA_TSTRING) {
            obj = this.toString(idx);
        } else if (this.isFunction(idx)) {
            obj = this.getLuaObject(idx);
        } else if (this.isTable(idx)) {
            obj = this.getLuaObject(idx);
        } else if (this.type(idx) == LuaState.LUA_TNUMBER) {
            obj = new Double(this.toNumber(idx));
        } else if (this.isUserdata(idx)) {
            if (this.isObject(idx)) {
                obj = this.getObjectFromUserdata(idx);
            } else {
                obj = this.getLuaObject(idx);
            }
        } else if (this.isNil(idx)) {
            obj = null;
        }

        return obj;
    }

    /**
     * Creates a reference to an object in the variable globalName
     * @param globalName
     * @return LuaObject
     */
    public LuaObject getLuaObject(String globalName) {
        return new LuaObject(this, globalName);
    }

    /**
     * Creates a reference to an object inside another object
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name that index the field
     * @return LuaObject
     * @throws LuaException
     *             if parent is not a table or userdata
     */
    public LuaObject getLuaObject(LuaObject parent, String name)
            throws LuaException {
        if (parent.L.getCPtrPeer() != this.luaState.getPeer()) {
            throw new LuaException(
                    "Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by a
     * number.
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name (number) that index the field
     * @return LuaObject
     * @throws LuaException
     *             When the parent object isn't a Table or Userdata
     */
    public LuaObject getLuaObject(LuaObject parent, Number name)
            throws LuaException {
        if (parent.L.getCPtrPeer() != this.luaState.getPeer()) {
            throw new LuaException(
                    "Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by any
     * LuaObject.
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name (LuaObject) that index the field
     * @return LuaObject
     * @throws LuaException
     *             When the parent object isn't a Table or Userdata
     */
    public LuaObject getLuaObject(LuaObject parent, LuaObject name)
            throws LuaException {
        if (parent.getLuaState().getCPtrPeer() != this.luaState.getPeer()
                || parent.getLuaState().getCPtrPeer() != name.getLuaState()
                        .getCPtrPeer()) {
            throw new LuaException(
                    "Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * Creates a reference to an object in the <code>index</code> position
     * of the stack
     * @param index
     *            position on the stack
     * @return LuaObject
     */
    public LuaObject getLuaObject(int index) {
        return new LuaObject(this, index);
    }

    /**
     * When you call a function in lua, it may return a number, and the
     * number will be interpreted as a <code>Double</code>.<br>
     * This function converts the number into a type specified by
     * <code>retType</code>
     * @param db
     *            lua number to be converted
     * @param retType
     *            type to convert to
     * @return The converted number
     */
    public static Number convertLuaNumber(Double db, Class retType) {
        // checks if retType is a primitive type
        if (retType.isPrimitive()) {
            if (retType == Integer.TYPE) {
                return new Integer(db.intValue());
            } else if (retType == Long.TYPE) {
                return new Long(db.longValue());
            } else if (retType == Float.TYPE) {
                return new Float(db.floatValue());
            } else if (retType == Double.TYPE) {
                return db;
            } else if (retType == Byte.TYPE) {
                return new Byte(db.byteValue());
            } else if (retType == Short.TYPE) {
                return new Short(db.shortValue());
            }
        } else if (retType.isAssignableFrom(Number.class)) {
            // Checks all possibilities of number types
            if (retType.isAssignableFrom(Integer.class)) {
                return new Integer(db.intValue());
            } else if (retType.isAssignableFrom(Long.class)) {
                return new Long(db.longValue());
            } else if (retType.isAssignableFrom(Float.class)) {
                return new Float(db.floatValue());
            } else if (retType.isAssignableFrom(Double.class)) {
                return db;
            } else if (retType.isAssignableFrom(Byte.class)) {
                return new Byte(db.byteValue());
            } else if (retType.isAssignableFrom(Short.class)) {
                return new Short(db.shortValue());
            }
        }

        // if all checks fail, return null
        return null;
    }

    public String dumpStack() {
        int n = this.getTop();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            int t = this.type(i);
            sb.append(i).append(": ").append(this.typeName(t));
            if (t == LUA_TNUMBER) {
                sb.append(" = ").append(this.toNumber(i));
            } else if (t == LUA_TSTRING) {
                sb.append(" = '").append(this.toString(i)).append("'");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
