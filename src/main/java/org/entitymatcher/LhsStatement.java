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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * This class represents a statement that takes only one table and column.
 * <p>
 * It takes the form : LEFT_HAND_SIDE_TABLE_AND_COLUMN OPERATOR [VALUE | VARIABLE]
 * <p>
 * This statement class allows applying different operators and [value | variable]'s to the same
 * LEFT_HAND_SIDE_TABLE_AND_COLUMN through the {@link #and(LhsStatement)} and
 * {@link #or(LhsStatement)} methods.
 * <p>
 * The table alias, as well as the column are passed to the
 * {@link #toJpql(String, String, String, String)} via lhs args.
 */
public class LhsStatement<T> extends LhsRhsStatement<T>
{
    private final Statement[] statements;

    public LhsStatement(Statement statement)
    {
        super(null, false);
        this.statements = new Statement[] { statement };
    }

    private LhsStatement(Statement... statements)
    {
        super(null, false);
        this.statements = statements;
    }

    public LhsStatement<T> or(LhsStatement<T> st)
    {
        final Statement[] composition = new Statement[statements.length + 2];
        System.arraycopy(statements, 0, composition, 0, statements.length);
        composition[composition.length - 2] = or;
        composition[composition.length - 1] = st;

        return new LhsStatement<T>(composition);
    }

    public LhsStatement<T> and(LhsStatement<T> st)
    {
        final Statement[] composition = new Statement[statements.length + 2];
        System.arraycopy(statements, 0, composition, 0, statements.length);
        composition[composition.length - 2] = and;
        composition[composition.length - 1] = st;

        return new LhsStatement<T>(composition);
    }

    @Override
    public List<Part> toJpql(String lhsTable, String lhsColumn, String rhsTable, String rhsColumn, ParameterBinding params)
    {
        final List<Part> l = new ArrayList<Part>();
        for (Statement st : statements)
        {
            l.addAll(st.toJpql(lhsTable, lhsColumn, rhsTable, rhsColumn, params));
        }
        return l;
    }

    // Ignore OR / AND negation
    enum Connector implements Negatable
    {
        OR, AND;

        @Override
        public void negate()
        {
        }
    }

    public static final LhsStatement<?> or = new LhsStatement<Object>(
            (lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Lists.newArrayList(Statement.create(" ", Connector.OR, " ")));
    public static final LhsStatement<?> and = new LhsStatement<Object>(
            (lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Lists.newArrayList(Statement.create(" ", Connector.AND, " ")));
}
