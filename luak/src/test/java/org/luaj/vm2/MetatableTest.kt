/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2

import junit.framework.TestCase

import org.luaj.vm2.TypeTest.MyData
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

class MetatableTest : TestCase() {

    private val samplestring = "abcdef"
    private val sampleobject = Any()
    private val sampledata = MyData()

    private val string = LuaValue.valueOf(samplestring)
    private val table = LuaValue.tableOf()
    private val function = object : ZeroArgFunction() {
        override fun call(): LuaValue {
            return LuaValue.NONE
        }
    }
    private val thread = LuaThread(Globals(), function)
    private val closure = LuaClosure(Prototype(), LuaTable())
    private val userdata = LuaValue.userdataOf(sampleobject)
    private val userdatamt = LuaValue.userdataOf(sampledata, table)

    @Throws(Exception::class)
    override fun setUp() {
        // needed for metatable ops to work on strings
        StringLib()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
        LuaBoolean.s_metatable = null
        LuaFunction.s_metatable = null
        LuaNil.s_metatable = null
        LuaNumber.s_metatable = null
        //		LuaString.s_metatable = null;
        LuaThread.s_metatable = null
    }

    fun testGetMetatable() {
        TestCase.assertEquals(null, LuaValue.NIL.getmetatable())
        TestCase.assertEquals(null, LuaValue.TRUE.getmetatable())
        TestCase.assertEquals(null, LuaValue.ONE.getmetatable())
        //		assertEquals( null, string.getmetatable() );
        TestCase.assertEquals(null, table.getmetatable())
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        TestCase.assertEquals(null, userdata.getmetatable())
        TestCase.assertEquals(table, userdatamt.getmetatable())
    }

    fun testSetMetatable() {
        val mt = LuaValue.tableOf()
        TestCase.assertEquals(null, table.getmetatable())
        TestCase.assertEquals(null, userdata.getmetatable())
        TestCase.assertEquals(table, userdatamt.getmetatable())
        TestCase.assertEquals(table, table.setmetatable(mt))
        TestCase.assertEquals(userdata, userdata.setmetatable(mt))
        TestCase.assertEquals(userdatamt, userdatamt.setmetatable(mt))
        TestCase.assertEquals(mt, table.getmetatable())
        TestCase.assertEquals(mt, userdata.getmetatable())
        TestCase.assertEquals(mt, userdatamt.getmetatable())

        // these all get metatable behind-the-scenes
        TestCase.assertEquals(null, LuaValue.NIL.getmetatable())
        TestCase.assertEquals(null, LuaValue.TRUE.getmetatable())
        TestCase.assertEquals(null, LuaValue.ONE.getmetatable())
        //		assertEquals( null, string.getmetatable() );
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        LuaNil.s_metatable = mt
        TestCase.assertEquals(mt, LuaValue.NIL.getmetatable())
        TestCase.assertEquals(null, LuaValue.TRUE.getmetatable())
        TestCase.assertEquals(null, LuaValue.ONE.getmetatable())
        //		assertEquals( null, string.getmetatable() );
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        LuaBoolean.s_metatable = mt
        TestCase.assertEquals(mt, LuaValue.TRUE.getmetatable())
        TestCase.assertEquals(null, LuaValue.ONE.getmetatable())
        //		assertEquals( null, string.getmetatable() );
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        LuaNumber.s_metatable = mt
        TestCase.assertEquals(mt, LuaValue.ONE.getmetatable())
        TestCase.assertEquals(mt, LuaValue.valueOf(1.25).getmetatable())
        //		assertEquals( null, string.getmetatable() );
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        //		LuaString.s_metatable = mt;
        //		assertEquals( mt, string.getmetatable() );
        TestCase.assertEquals(null, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        TestCase.assertEquals(null, closure.getmetatable())
        LuaFunction.s_metatable = mt
        TestCase.assertEquals(mt, function.getmetatable())
        TestCase.assertEquals(null, thread.getmetatable())
        LuaThread.s_metatable = mt
        TestCase.assertEquals(mt, thread.getmetatable())
    }

    fun testMetatableIndex() {
        TestCase.assertEquals(table, table.setmetatable(null))
        TestCase.assertEquals(userdata, userdata.setmetatable(null))
        TestCase.assertEquals(userdatamt, userdatamt.setmetatable(null))
        TestCase.assertEquals(LuaValue.NIL, table.get(1))
        TestCase.assertEquals(LuaValue.NIL, userdata.get(1))
        TestCase.assertEquals(LuaValue.NIL, userdatamt.get(1))

        // empty metatable
        val mt = LuaValue.tableOf()
        TestCase.assertEquals(table, table.setmetatable(mt))
        TestCase.assertEquals(userdata, userdata.setmetatable(mt))
        LuaBoolean.s_metatable = mt
        LuaFunction.s_metatable = mt
        LuaNil.s_metatable = mt
        LuaNumber.s_metatable = mt
        //		LuaString.s_metatable = mt;
        LuaThread.s_metatable = mt
        TestCase.assertEquals(mt, table.getmetatable())
        TestCase.assertEquals(mt, userdata.getmetatable())
        TestCase.assertEquals(mt, LuaValue.NIL.getmetatable())
        TestCase.assertEquals(mt, LuaValue.TRUE.getmetatable())
        TestCase.assertEquals(mt, LuaValue.ONE.getmetatable())
        // 		assertEquals( StringLib.instance, string.getmetatable() );
        TestCase.assertEquals(mt, function.getmetatable())
        TestCase.assertEquals(mt, thread.getmetatable())

        // plain metatable
        val abc = LuaValue.valueOf("abc")
        mt.set(LuaValue.INDEX, LuaValue.listOf(arrayOf<LuaValue>(abc)))
        TestCase.assertEquals(abc, table.get(1))
        TestCase.assertEquals(abc, userdata.get(1))
        TestCase.assertEquals(abc, LuaValue.NIL.get(1))
        TestCase.assertEquals(abc, LuaValue.TRUE.get(1))
        TestCase.assertEquals(abc, LuaValue.ONE.get(1))
        // 		assertEquals( abc, string.get(1) );
        TestCase.assertEquals(abc, function.get(1))
        TestCase.assertEquals(abc, thread.get(1))

        // plain metatable
        mt.set(LuaValue.INDEX, object : TwoArgFunction() {
            override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
                return LuaValue.valueOf(arg1.typename() + "[" + arg2.tojstring() + "]=xyz")
            }

        })
        TestCase.assertEquals("table[1]=xyz", table.get(1).tojstring())
        TestCase.assertEquals("userdata[1]=xyz", userdata.get(1).tojstring())
        TestCase.assertEquals("nil[1]=xyz", LuaValue.NIL.get(1).tojstring())
        TestCase.assertEquals("boolean[1]=xyz", LuaValue.TRUE.get(1).tojstring())
        TestCase.assertEquals("number[1]=xyz", LuaValue.ONE.get(1).tojstring())
        //	assertEquals( "string[1]=xyz",   string.get(1).tojstring() );
        TestCase.assertEquals("function[1]=xyz", function.get(1).tojstring())
        TestCase.assertEquals("thread[1]=xyz", thread.get(1).tojstring())
    }


    fun testMetatableNewIndex() {
        // empty metatable
        val mt = LuaValue.tableOf()
        TestCase.assertEquals(table, table.setmetatable(mt))
        TestCase.assertEquals(userdata, userdata.setmetatable(mt))
        LuaBoolean.s_metatable = mt
        LuaFunction.s_metatable = mt
        LuaNil.s_metatable = mt
        LuaNumber.s_metatable = mt
        //		LuaString.s_metatable = mt;
        LuaThread.s_metatable = mt

        // plain metatable
        val fallback = LuaValue.tableOf()
        val abc = LuaValue.valueOf("abc")
        mt.set(LuaValue.NEWINDEX, fallback)
        table.set(2, abc)
        userdata.set(3, abc)
        LuaValue.NIL.set(4, abc)
        LuaValue.TRUE.set(5, abc)
        LuaValue.ONE.set(6, abc)
        // 		string.set(7,abc);
        function.set(8, abc)
        thread.set(9, abc)
        TestCase.assertEquals(abc, fallback.get(2))
        TestCase.assertEquals(abc, fallback.get(3))
        TestCase.assertEquals(abc, fallback.get(4))
        TestCase.assertEquals(abc, fallback.get(5))
        TestCase.assertEquals(abc, fallback.get(6))
        // 		assertEquals( abc, StringLib.instance.get(7) );
        TestCase.assertEquals(abc, fallback.get(8))
        TestCase.assertEquals(abc, fallback.get(9))

        // metatable with function call
        mt.set(LuaValue.NEWINDEX, object : ThreeArgFunction() {
            override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
                fallback.rawset(arg2, LuaValue.valueOf("via-func-$arg3"))
                return LuaValue.NONE
            }

        })
        table.set(12, abc)
        userdata.set(13, abc)
        LuaValue.NIL.set(14, abc)
        LuaValue.TRUE.set(15, abc)
        LuaValue.ONE.set(16, abc)
        // 		string.set(17,abc);
        function.set(18, abc)
        thread.set(19, abc)
        val via = LuaValue.valueOf("via-func-abc")
        TestCase.assertEquals(via, fallback.get(12))
        TestCase.assertEquals(via, fallback.get(13))
        TestCase.assertEquals(via, fallback.get(14))
        TestCase.assertEquals(via, fallback.get(15))
        TestCase.assertEquals(via, fallback.get(16))
        //		assertEquals( via, StringLib.instance.get(17) );
        TestCase.assertEquals(via, fallback.get(18))
        TestCase.assertEquals(via, fallback.get(19))
    }


    private fun checkTable(
        t: LuaValue,
        aa: LuaValue, bb: LuaValue, cc: LuaValue, dd: LuaValue, ee: LuaValue, ff: LuaValue, gg: LuaValue,
        ra: LuaValue, rb: LuaValue, rc: LuaValue, rd: LuaValue, re: LuaValue, rf: LuaValue, rg: LuaValue
    ) {
        TestCase.assertEquals(aa, t.get("aa"))
        TestCase.assertEquals(bb, t.get("bb"))
        TestCase.assertEquals(cc, t.get("cc"))
        TestCase.assertEquals(dd, t.get("dd"))
        TestCase.assertEquals(ee, t.get("ee"))
        TestCase.assertEquals(ff, t.get("ff"))
        TestCase.assertEquals(gg, t.get("gg"))
        TestCase.assertEquals(ra, t.rawget("aa"))
        TestCase.assertEquals(rb, t.rawget("bb"))
        TestCase.assertEquals(rc, t.rawget("cc"))
        TestCase.assertEquals(rd, t.rawget("dd"))
        TestCase.assertEquals(re, t.rawget("ee"))
        TestCase.assertEquals(rf, t.rawget("ff"))
        TestCase.assertEquals(rg, t.rawget("gg"))
    }

    private fun makeTable(key1: String, val1: String, key2: String, val2: String): LuaValue {
        return LuaValue.tableOf(
            arrayOf<LuaValue>(
                LuaValue.valueOf(key1),
                LuaValue.valueOf(val1),
                LuaValue.valueOf(key2),
                LuaValue.valueOf(val2)
            )
        )
    }

    fun testRawsetMetatableSet() {
        // set up tables
        val m = makeTable("aa", "aaa", "bb", "bbb")
        m.set(LuaValue.INDEX, m)
        m.set(LuaValue.NEWINDEX, m)
        val s = makeTable("cc", "ccc", "dd", "ddd")
        val t = makeTable("cc", "ccc", "dd", "ddd")
        t.setmetatable(m)
        val aaa = LuaValue.valueOf("aaa")
        val bbb = LuaValue.valueOf("bbb")
        val ccc = LuaValue.valueOf("ccc")
        val ddd = LuaValue.valueOf("ddd")
        val ppp = LuaValue.valueOf("ppp")
        val qqq = LuaValue.valueOf("qqq")
        val rrr = LuaValue.valueOf("rrr")
        val sss = LuaValue.valueOf("sss")
        val ttt = LuaValue.valueOf("ttt")
        val www = LuaValue.valueOf("www")
        val xxx = LuaValue.valueOf("xxx")
        val yyy = LuaValue.valueOf("yyy")
        val zzz = LuaValue.valueOf("zzz")
        val nil = LuaValue.NIL

        // check initial values
        //             values via "bet()"           values via "rawget()"
        checkTable(s, nil, nil, ccc, ddd, nil, nil, nil, nil, nil, ccc, ddd, nil, nil, nil)
        checkTable(t, aaa, bbb, ccc, ddd, nil, nil, nil, nil, nil, ccc, ddd, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)

        // rawset()
        s.rawset("aa", www)
        checkTable(s, www, nil, ccc, ddd, nil, nil, nil, www, nil, ccc, ddd, nil, nil, nil)
        checkTable(t, aaa, bbb, ccc, ddd, nil, nil, nil, nil, nil, ccc, ddd, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)
        s.rawset("cc", xxx)
        checkTable(s, www, nil, xxx, ddd, nil, nil, nil, www, nil, xxx, ddd, nil, nil, nil)
        checkTable(t, aaa, bbb, ccc, ddd, nil, nil, nil, nil, nil, ccc, ddd, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)
        t.rawset("bb", yyy)
        checkTable(s, www, nil, xxx, ddd, nil, nil, nil, www, nil, xxx, ddd, nil, nil, nil)
        checkTable(t, aaa, yyy, ccc, ddd, nil, nil, nil, nil, yyy, ccc, ddd, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)
        t.rawset("dd", zzz)
        checkTable(s, www, nil, xxx, ddd, nil, nil, nil, www, nil, xxx, ddd, nil, nil, nil)
        checkTable(t, aaa, yyy, ccc, zzz, nil, nil, nil, nil, yyy, ccc, zzz, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)

        // set() invoking metatables
        s.set("ee", ppp)
        checkTable(s, www, nil, xxx, ddd, ppp, nil, nil, www, nil, xxx, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, zzz, nil, nil, nil, nil, yyy, ccc, zzz, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)
        s.set("cc", qqq)
        checkTable(s, www, nil, qqq, ddd, ppp, nil, nil, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, zzz, nil, nil, nil, nil, yyy, ccc, zzz, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, nil, nil, aaa, bbb, nil, nil, nil, nil, nil)
        t.set("ff", rrr)
        checkTable(s, www, nil, qqq, ddd, ppp, nil, nil, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, zzz, nil, rrr, nil, nil, yyy, ccc, zzz, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, nil, aaa, bbb, nil, nil, nil, rrr, nil)
        t.set("dd", sss)
        checkTable(s, www, nil, qqq, ddd, ppp, nil, nil, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, sss, nil, rrr, nil, nil, yyy, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, nil, aaa, bbb, nil, nil, nil, rrr, nil)
        m.set("gg", ttt)
        checkTable(s, www, nil, qqq, ddd, ppp, nil, nil, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, sss, nil, rrr, ttt, nil, yyy, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, ttt, aaa, bbb, nil, nil, nil, rrr, ttt)

        // make s fall back to t
        s.setmetatable(LuaValue.tableOf(arrayOf(LuaValue.INDEX, t, LuaValue.NEWINDEX, t)))
        checkTable(s, www, yyy, qqq, ddd, ppp, rrr, ttt, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, sss, nil, rrr, ttt, nil, yyy, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, ttt, aaa, bbb, nil, nil, nil, rrr, ttt)
        s.set("aa", www)
        checkTable(s, www, yyy, qqq, ddd, ppp, rrr, ttt, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, yyy, ccc, sss, nil, rrr, ttt, nil, yyy, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, ttt, aaa, bbb, nil, nil, nil, rrr, ttt)
        s.set("bb", zzz)
        checkTable(s, www, zzz, qqq, ddd, ppp, rrr, ttt, www, nil, qqq, ddd, ppp, nil, nil)
        checkTable(t, aaa, zzz, ccc, sss, nil, rrr, ttt, nil, zzz, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, ttt, aaa, bbb, nil, nil, nil, rrr, ttt)
        s.set("ee", xxx)
        checkTable(s, www, zzz, qqq, ddd, xxx, rrr, ttt, www, nil, qqq, ddd, xxx, nil, nil)
        checkTable(t, aaa, zzz, ccc, sss, nil, rrr, ttt, nil, zzz, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, rrr, ttt, aaa, bbb, nil, nil, nil, rrr, ttt)
        s.set("ff", yyy)
        checkTable(s, www, zzz, qqq, ddd, xxx, yyy, ttt, www, nil, qqq, ddd, xxx, nil, nil)
        checkTable(t, aaa, zzz, ccc, sss, nil, yyy, ttt, nil, zzz, ccc, sss, nil, nil, nil)
        checkTable(m, aaa, bbb, nil, nil, nil, yyy, ttt, aaa, bbb, nil, nil, nil, yyy, ttt)


    }

}
