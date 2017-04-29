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
 * This class represents a statement that takes two tables and two columns.
 * <p>
 * It takes the form : LEFT_HAND_SIDE_TABLE_AND_COLUMN OPERATOR RIGHT_HAND_SIDE_TABLE_AND_COLUMN
 * <p>
 * The table alias, as well as the column are passed to the
 * {@link #toJpql(String, String, String, String)} via lhs / rhs args.
 */
public class LhsRhsStatement<T> implements JpqlStatement
{
    private final JpqlStatement statement;

    // Identifies INNER / OUTER / FETCH joins which must be placed in the FROM clause (see JPA 2.0 specification). 
    private final boolean relationship; 
    
    public LhsRhsStatement(JpqlStatement statement, boolean relationship)
    {
        this.statement = statement;
        this.relationship = relationship;
    }
    
    @Override
    public String toJpql(String lhsTable, String lhsColumn, String rhsTable, String rhsColumn)
    {
        return statement.toJpql(lhsTable, lhsColumn, rhsTable, rhsColumn);
    }

    protected boolean isJoinRelationship()
    {
        return relationship;
    }
}
