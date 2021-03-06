/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.luaj.vm2.compiler

import java.io.IOException
import java.io.InputStream

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Prototype
import org.luaj.vm2.lib.BaseLib

/**
 * Compiler for Lua.
 *
 *
 *
 * Compiles lua source files into lua bytecode within a [Prototype],
 * loads lua binary files directly into a [Prototype],
 * and optionaly instantiates a [LuaClosure] around the result
 * using a user-supplied environment.
 *
 *
 *
 * Implements the [org.luaj.vm2.Globals.Compiler] interface for loading
 * initialized chunks, which is an interface common to
 * lua bytecode compiling and java bytecode compiling.
 *
 *
 *
 * The [LuaC] compiler is installed by default by both the
 * [org.luaj.vm2.lib.jse.JsePlatform] and [org.luaj.vm2.lib.jme.JmePlatform] classes,
 * so in the following example, the default [LuaC] compiler
 * will be used:
 * <pre> `Globals globals = JsePlatform.standardGlobals();
 * globals.load(new StringReader("print 'hello'"), "main.lua" ).call();
` *  </pre>
 *
 * To load the LuaC compiler manually, use the install method:
 * <pre> `LuaC.install(globals);
` *  </pre>
 *
 * @see .install
 * @see Globals.compiler
 *
 * @see Globals.loader
 *
 * @see org.luaj.vm2.luajc.LuaJC
 *
 * @see org.luaj.vm2.lib.jse.JsePlatform
 *
 * @see org.luaj.vm2.lib.jme.JmePlatform
 *
 * @see BaseLib
 *
 * @see LuaValue
 *
 * @see Prototype
 */
class LuaC protected constructor() : Constants(), Globals.Compiler, Globals.Loader {

    /** Compile lua source into a Prototype.
     * @param stream InputStream representing the text source conforming to lua source syntax.
     * @param chunkname String name of the chunk to use.
     * @return Prototype representing the lua chunk for this source.
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun compile(stream: InputStream, chunkname: String): Prototype {
        return CompileState().luaY_parser(stream, chunkname)
    }

    @Throws(IOException::class)
    override fun load(prototype: Prototype, chunkname: String, env: LuaValue): LuaFunction {
        return LuaClosure(prototype, env)
    }


    @Deprecated(
        " Use Globals.load(InputString, String, String) instead, \n" +
                "\t  or LuaC.compile(InputStream, String) and construct LuaClosure directly."
    )
    @Throws(IOException::class)
    fun load(stream: InputStream, chunkname: String, globals: Globals): LuaValue {
        return LuaClosure(compile(stream, chunkname), globals)
    }

    class CompileState {
        @JvmField
        var nCcalls = 0
        private val strings = HashMap<LuaString, LuaString>()

        /** Parse the input  */
        @Throws(IOException::class)
        fun luaY_parser(z: InputStream, name: String): Prototype {
            val lexstate = LexState(this, z)
            val funcstate = FuncState()
            // lexstate.buff = buff;
            lexstate.fs = funcstate
            lexstate.setinput(this, z.read(), z, LuaValue.valueOf(name) as LuaString)
            /* main func. is always vararg */
            funcstate.f = Prototype()
            funcstate.f!!.source = LuaValue.valueOf(name) as LuaString
            lexstate.mainfunc(funcstate)
            Constants._assert(funcstate.prev == null)
            /* all scopes should be correctly finished */
            Constants._assert(lexstate.dyd == null || lexstate.dyd.n_actvar == 0 && lexstate.dyd.n_gt == 0 && lexstate.dyd.n_label == 0)
            return funcstate.f!!
        }

        // look up and keep at most one copy of each string
        fun newTString(s: String): LuaString {
            return cachedLuaString(LuaString.valueOf(s))
        }

        // look up and keep at most one copy of each string
        fun newTString(s: LuaString): LuaString {
            return cachedLuaString(s)
        }

        fun cachedLuaString(s: LuaString): LuaString {
            val c = strings.get(s) as? LuaString?
            if (c != null)
                return c
            strings.put(s, s)
            return s
        }

        fun pushfstring(string: String): String {
            return string
        }
    }

    companion object {

        /** A sharable instance of the LuaC compiler.  */
        @JvmField
        val instance = LuaC()

        /** Install the compiler so that LoadState will first
         * try to use it when handed bytes that are
         * not already a compiled lua chunk.
         * @param globals the Globals into which this is to be installed.
         */
        @JvmStatic
        fun install(globals: Globals) {
            globals.compiler = instance
            globals.loader = instance
        }
    }
}
