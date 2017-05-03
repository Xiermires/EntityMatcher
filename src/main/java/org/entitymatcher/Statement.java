/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.entitymatcher;

import java.util.List;

import com.google.common.collect.Lists;

public interface Statement
{
    List<Part> toJpql(String lhsTableAlias, String lhsColumn, String rhsTableAlias, String rhsColumn, ParameterBinding params);

    public static List<Part> create(String expr)
    {
        return Lists.newArrayList(new Part(expr, dontNegate, ""));
    }

    public static List<Part> create(String lhs, Negatable conn, String rhs)
    {
        return Lists.newArrayList(new Part(lhs, conn, rhs));
    }

    public static String toString(List<Part> parts)
    {
        final StringBuilder sb = new StringBuilder();
        for (Part part : parts)
            sb.append(toString(part));
        return sb.toString();
    }

    public static String toString(Part part)
    {
        return part.lhs + part.conn.toString() + part.rhs;
    }

    public static class Part
    {
        final String lhs;
        final Negatable conn;
        final String rhs;

        Part(String lhs, Negatable conn, String rhs)
        {
            this.lhs = lhs;
            this.conn = conn;
            this.rhs = rhs;
        }
    }
    
    /**
     * Negates an expression.
     * <p>
     * The query string uses its {@link #toString()} representation.
     */
    public static interface Negatable
    {
        void negate();
    }

    public static final Negatable dontNegate = new Negatable()
    {
        @Override
        public void negate()
        {
        }

        @Override
        public String toString()
        {
            return "";
        }
    };
}
