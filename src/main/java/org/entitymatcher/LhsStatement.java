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
    private final JpqlStatement[] statements;

    public LhsStatement(JpqlStatement statement)
    {
        super(null, false);
        this.statements = new JpqlStatement[] { statement };
    }

    private LhsStatement(JpqlStatement... statements)
    {
        super(null, false);
        this.statements = statements;
    }

    public LhsStatement<T> or(LhsStatement<T> st)
    {
        final JpqlStatement[] composition = new JpqlStatement[statements.length + 2];
        System.arraycopy(statements, 0, composition, 0, statements.length);
        composition[composition.length - 2] = or;
        composition[composition.length - 1] = st;

        return new LhsStatement<T>(composition);
    }

    public LhsStatement<T> and(LhsStatement<T> st)
    {
        final JpqlStatement[] composition = new JpqlStatement[statements.length + 2];
        System.arraycopy(statements, 0, composition, 0, statements.length);
        composition[composition.length - 2] = and;
        composition[composition.length - 1] = st;

        return new LhsStatement<T>(composition);
    }

    @Override
    public String toJpql(String lhsTable, String lhsColumn, String rhsTable, String rhsColumn)
    {
        final StringBuilder sb = new StringBuilder();
        for (JpqlStatement st : statements)
        {
            sb.append(st.toJpql(lhsTable, lhsColumn, rhsTable, rhsColumn));
        }
        return sb.toString();
    }

    public static final LhsStatement<?> or = new LhsStatement<Object>((lhsTable, lhsColumn, rhsTable, rhsColumn) -> " OR ");
    public static final LhsStatement<?> and = new LhsStatement<Object>((lhsTable, lhsColumn, rhsTable, rhsColumn) -> " AND ");
}
