/*******************************************************************************
 * Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.ast

import org.luaj.vm2.LuaString

class FuncArgs : SyntaxElement {

    @JvmField
    val exps: MutableList<Exp>?

    constructor(exps: MutableList<Exp>?) {
        this.exps = exps
    }

    constructor(string: LuaString) {
        this.exps = ArrayList()
        this.exps.add(Exp.constant(string))
    }

    constructor(table: TableConstructor) {
        this.exps = ArrayList()
        this.exps.add(table)
    }

    fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    companion object {

        /** exp1,exp2...  */
        @JvmStatic
        fun explist(explist: MutableList<Exp>?): FuncArgs {
            return FuncArgs(explist)
        }

        /** {...}  */
        @JvmStatic
        fun tableconstructor(table: TableConstructor): FuncArgs {
            return FuncArgs(table)
        }

        /** "mylib"  */
        @JvmStatic
        fun string(string: LuaString): FuncArgs {
            return FuncArgs(string)
        }
    }

}
