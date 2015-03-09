/*
 * $Id: LuaObject.java,v 1.6 2006/12/22 14:06:40 thiago Exp $
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.StringTokenizer;

/**
 * This class represents a Lua object of any type. A LuaObject is constructed by
 * a {@link LuaState} object using one of
 * the four methods:
 * <ul>
 * <li>{@link LuaState#getLuaObject(String globalName)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, String name)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, Number name)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, LuaObject name)}</li>
 * <li>{@link LuaState#getLuaObject(int index)}</li>
 * </ul>
 * The LuaObject will represent only the object itself, not a variable or a
 * stack index, so when you change a string,
 * remember that strings are immutable objects in Lua, and the LuaObject you
 * have will represent the old one.
 * <h2>Proxies</h2>
 * LuaJava allows you to implement a class in Lua, like said before. If you want
 * to create this proxy from Java, you
 * should have a LuaObject representing the table that has the functions that
 * implement the interface. From this
 * LuaObject you can call the <code>createProxy(String implements)</code>. This
 * method receives the string with the
 * name of the interfaces implemented by the object separated by comma.
 * @author Rizzato
 * @author Thiago Ponte
 */
public class LuaObject {
    protected Integer ref;

    protected LuaState L;

    /**
     * Creates a reference to an object in the variable globalName
     * @param L
     * @param globalName
     */
    protected LuaObject(LuaState L, String globalName) {
        synchronized (L) {
            this.L = L;
            L.getGlobal(globalName);
            this.registerValue(-1);
            L.pop(1);
        }
    }

    /**
     * Creates a reference to an object inside another object
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name that index the field
     */
    protected LuaObject(LuaObject parent, String name) throws LuaException {
        synchronized (parent.getLuaState()) {
            this.L = parent.getLuaState();

            if (!parent.isTable() && !parent.isUserdata()) {
                throw new LuaException(
                        "Object parent should be a table or userdata .");
            }

            parent.push();
            this.L.pushString(name);
            this.L.getTable(-2);
            this.L.remove(-2);
            this.registerValue(-1);
            this.L.pop(1);
        }
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by a
     * number.
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name (number) that index the field
     * @throws LuaException
     *             When the parent object isn't a Table or Userdata
     */
    protected LuaObject(LuaObject parent, Number name) throws LuaException {
        synchronized (parent.getLuaState()) {
            this.L = parent.getLuaState();
            if (!parent.isTable() && !parent.isUserdata()) {
                throw new LuaException(
                        "Object parent should be a table or userdata .");
            }

            parent.push();
            this.L.pushNumber(name.doubleValue());
            this.L.getTable(-2);
            this.L.remove(-2);
            this.registerValue(-1);
            this.L.pop(1);
        }
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by a
     * LuaObject.
     * @param parent
     *            The Lua Table or Userdata that contains the Field.
     * @param name
     *            The name (LuaObject) that index the field
     * @throws LuaException
     *             When the parent object isn't a Table or Userdata
     */
    protected LuaObject(LuaObject parent, LuaObject name) throws LuaException {
        if (parent.getLuaState() != name.getLuaState()) {
            throw new LuaException("LuaStates must be the same!");
        }
        synchronized (parent.getLuaState()) {
            if (!parent.isTable() && !parent.isUserdata()) {
                throw new LuaException(
                        "Object parent should be a table or userdata .");
            }

            this.L = parent.getLuaState();

            parent.push();
            name.push();
            this.L.getTable(-2);
            this.L.remove(-2);
            this.registerValue(-1);
            this.L.pop(1);
        }
    }

    /**
     * Creates a reference to an object in the given index of the stack
     * @param L
     * @param index
     *            of the object on the lua stack
     */
    protected LuaObject(LuaState L, int index) {
        synchronized (L) {
            this.L = L;

            this.registerValue(index);
        }
    }

    /**
     * Gets the Object's State
     */
    public LuaState getLuaState() {
        return this.L;
    }

    /**
     * Creates the reference to the object in the registry table
     * @param index
     *            of the object on the lua stack
     */
    private void registerValue(int index) {
        synchronized (this.L) {
            this.L.pushValue(index);
            int key = this.L.Lref(LuaState.LUA_REGISTRYINDEX);
            this.ref = new Integer(key);
        }
    }

    @Override
    protected void finalize() {
        try {
            synchronized (this.L) {
                if (this.L.getCPtrPeer() != 0) {
                    this.L.LunRef(LuaState.LUA_REGISTRYINDEX,
                            this.ref.intValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to release object " + this.ref);
        }
    }

    /**
     * Pushes the object represented by <code>this<code> into L's stack
     */
    public void push() {
        this.L.rawGetI(LuaState.LUA_REGISTRYINDEX,
                this.ref.intValue());
    }

    public boolean isNil() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isNil(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isBoolean() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isBoolean(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isNumber() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isNumber(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isString() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isString(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isFunction() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isFunction(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isJavaObject() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isObject(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isJavaFunction() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isJavaFunction(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isTable() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isTable(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public boolean isUserdata() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.isUserdata(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public int type() {
        synchronized (this.L) {
            this.push();
            int type = this.L.type(-1);
            this.L.pop(1);
            return type;
        }
    }

    public boolean getBoolean() {
        synchronized (this.L) {
            this.push();
            boolean bool = this.L.toBoolean(-1);
            this.L.pop(1);
            return bool;
        }
    }

    public double getNumber() {
        synchronized (this.L) {
            this.push();
            double db = this.L.toNumber(-1);
            this.L.pop(1);
            return db;
        }
    }

    public String getString() {
        synchronized (this.L) {
            this.push();
            String str = this.L.toString(-1);
            this.L.pop(1);
            return str;
        }
    }

    public Object getObject() throws LuaException {
        synchronized (this.L) {
            this.push();
            Object obj = this.L.getObjectFromUserdata(-1);
            this.L.pop(1);
            return obj;
        }
    }

    /**
     * If <code>this<code> is a table or userdata tries to set
     * a field value.
     */
    public LuaObject getField(String field) throws LuaException {
        return this.L.getLuaObject(this, field);
    }

    /**
     * Calls the object represented by <code>this</code> using Lua function
     * pcall.
     * @param args
     *            -
     *            Call arguments
     * @param nres
     *            -
     *            Number of objects returned
     * @return Object[] - Returned Objects
     * @throws LuaException
     */
    public Object[] call(Object[] args, int nres) throws LuaException {
        synchronized (this.L) {
            if (!this.isFunction() && !this.isTable() && !this.isUserdata()) {
                throw new LuaException(
                        "Invalid object. Not a function, table or userdata .");
            }

            int top = this.L.getTop();
            this.push();
            int nargs;
            if (args != null) {
                nargs = args.length;
                for (int i = 0; i < nargs; i++) {
                    Object obj = args[i];
                    this.L.pushObjectValue(obj);
                }
            } else {
                nargs = 0;
            }

            int err = this.L.pcall(nargs, nres, 0);

            if (err != 0) {
                String str;
                if (this.L.isString(-1)) {
                    str = this.L.toString(-1);
                    this.L.pop(1);
                } else {
                    str = "";
                }

                if (err == LuaState.LUA_ERRRUN) {
                    str = "Runtime error. " + str;
                } else if (err == LuaState.LUA_ERRMEM) {
                    str = "Memory allocation error. " + str;
                } else if (err == LuaState.LUA_ERRERR) {
                    str = "Error while running the error handler function. "
                            + str;
                } else {
                    str = "Lua Error code " + err + ". " + str;
                }

                throw new LuaException(str);
            }

            if (nres == LuaState.LUA_MULTRET) {
                nres = this.L.getTop() - top;
            }
            if (this.L.getTop() - top < nres) {
                throw new LuaException("Invalid Number of Results .");
            }

            Object[] res = new Object[nres];

            for (int i = nres; i > 0; i--) {
                res[i - 1] = this.L.toJavaObject(-1);
                this.L.pop(1);
            }
            return res;
        }
    }

    /**
     * Calls the object represented by <code>this</code> using Lua function
     * pcall. Returns 1 object
     * @param args
     *            -
     *            Call arguments
     * @return Object - Returned Object
     * @throws LuaException
     */
    public Object call(Object[] args) throws LuaException {
        return this.call(args, 1)[0];
    }

    @Override
    public String toString() {
        synchronized (this.L) {
            try {
                if (this.isNil()) {
                    return "nil";
                } else if (this.isBoolean()) {
                    return String.valueOf(this.getBoolean());
                } else if (this.isNumber()) {
                    return String.valueOf(this.getNumber());
                } else if (this.isString()) {
                    return this.getString();
                } else if (this.isFunction()) {
                    return "Lua Function";
                } else if (this.isJavaObject()) {
                    return this.getObject().toString();
                } else if (this.isUserdata()) {
                    return "Userdata";
                } else if (this.isTable()) {
                    return "Lua Table";
                } else if (this.isJavaFunction()) {
                    return "Java Function";
                } else {
                    return null;
                }
            } catch (LuaException e) {
                return null;
            }
        }
    }

    /**
     * Function that creates a java proxy to the object represented by
     * <code>this</code>
     * @param implem
     *            Interfaces that are implemented, separated by <code>,</code>
     */
    public Object createProxy(String implem) throws ClassNotFoundException,
            LuaException {
        synchronized (this.L) {
            if (!this.isTable()) {
                throw new LuaException("Invalid Object. Must be Table.");
            }

            StringTokenizer st = new StringTokenizer(implem, ",");
            Class[] interfaces = new Class[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                interfaces[i] = Class.forName(st.nextToken());
            }

            InvocationHandler handler = new LuaInvocationHandler(this);

            return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    interfaces, handler);
        }
    }
}
