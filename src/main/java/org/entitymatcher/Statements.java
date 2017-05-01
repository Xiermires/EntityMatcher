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

import java.util.Collection;
import java.util.List;

import org.entitymatcher.Statement.Negatable;
import org.entitymatcher.Statement.Part;

public class Statements
{
    enum Connector implements Negatable
    {
        LIKE(" LIKE ", " NOT LIKE "), EQ(" = ", " != "), GT(" > ", " < "), LT(" < ", " > "), IN(" IN ", " NOT IN ");

        final String affirmed;
        final String negated;

        boolean isNegated;
        String conn;

        Connector(String affirmed, String negated)
        {
            this.affirmed = this.conn = affirmed;
            this.negated = negated;
            this.isNegated = false;
        }

        @Override
        public void negate()
        {
            conn = isNegated ? affirmed : negated;
        }

        @Override
        public String toString()
        {
            return conn;
        }
    }

    private Statements()
    {
    }

    public static <T> LhsStatement<T> like(T t)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn),
                Connector.LIKE, valueOf(t)));
    }

    public static <T> LhsStatement<T> eq(T t)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn),
                Connector.EQ, valueOf(t)));
    }

    public static <T> LhsStatement<T> gt(T t)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.GT, valueOf(t)));
    }

    public static <T> LhsStatement<T> lt(T t)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.LT,
                valueOf(t)));
    }

    public static <T> LhsStatement<T> in(Collection<T> ts)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.IN, params.bind(ts)));
    }

    public static <T> LhsRhsStatement<T> join(T getter)
    {
        return new LhsRhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.EQ, tableColumn(rhsTable, rhsColumn)), false);
    }

    public static <T> LhsStatement<T> not(LhsStatement<T> st)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) ->
        {
            final List<Part> parts = st.toJpql(lhsTable, lhsColumn, rhsTable, rhsColumn, params);
            for (Part part : parts)
                part.conn.negate();
            return parts;
        });
    }

    static String tableColumn(String table, String column)
    {
        return table.concat(".").concat(column);
    }

    static <T> String valueOf(T t)
    {
        if (t instanceof String)
        {
            return "'" + t + "'";
        }
        return String.valueOf(t);
    }
}