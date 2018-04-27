/*******************************************************************************
 * Copyright (c) 2018, Xavier Miret Andres <xavier.mires@gmail.com>
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

import static org.entitymatcher.NameBasedExpressionBuilder.tableColumn;

import java.util.Collection;

import org.entitymatcher.NameBasedExpressionBuilder.ConnectorExpression;
import org.entitymatcher.NameBasedExpressionBuilder.Expression;

import com.google.common.collect.Lists;

public class NameBasedExpressions
{
    static final Connector NONE = new OnlyConnector("", "");
    static final Connector OR = new OnlyConnector(" OR ", " OR ");
    static final Connector AND = new OnlyConnector(" AND ", " AND ");
    static final Connector OPEN = new OnlyConnector(" ( ", " ( ");
    static final Connector CLOSE = new OnlyConnector(" ) ", " ) ");

    static final Connector LIKE()
    {
        return new Connector(" LIKE ", " NOT LIKE ");
    }

    static final Connector EQ()
    {
        return new Connector(" = ", " != ")
        {
            @Override
            public String createStringExpression(String lhs, ParameterBinding bindings, Object param)
            {
                if (param == null)
                {
                    return lhs + (isNegated ? " IS NOT NULL " : " IS NULL ");
                }
                else return super.createStringExpression(lhs, bindings, param);
            }
        };
    }

    static final Connector GT()
    {
        return new Connector(" > ", " < ");
    }

    static final Connector LT()
    {
        return new Connector(" < ", " > ");
    }

    static final Connector IN()
    {
        return new Connector(" IN ", " NOT IN ");
    }

    static final Connector BETWEEN()
    {
        return new Connector(" BETWEEN ", " NOT BETWEEN ")
        {
            @Override
            public String createStringExpression(String lhs, ParameterBinding bindings, Object param)
            {
                assert param instanceof Object[];
                assert ((Object[]) param).length == 2;

                final Object[] boundaries = (Object[]) param;
                return lhs + conn + bindings.createParam(boundaries[0]) + " AND " + bindings.createParam(boundaries[1]);
            }
        };
    };

    // matchers

    /**
     * An inner join expression.
     * <p>
     * Equivalent to ?.? = other.property
     */
    public static ExpressionBuilder match(String property, Class<?> other)
    {
        final Expression expression = new Expression(EQ());
        expression.setJoinReferent(other);
        expression.setJoinProperty(property);
        final NameBasedExpressionBuilder builder = new NameBasedExpressionBuilder(expression);
        return match(null, property, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a column property.
     */
    public static ExpressionBuilder match(String property, ExpressionBuilder builder)
    {
        return match(null, property, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column
     * property.
     */
    public static ExpressionBuilder match(Class<?> referent, String property, ExpressionBuilder builder)
    {
        NameBasedExpressionBuilder.overwriteNullReferenceAndProperties(builder, referent, property);
        return builder;
    }

    // wrappers

    /**
     * Negates an expression by changing the sign of all {@link Negatable} connectors in the
     * {@code builder}.
     * <p>
     * i.e. {@code not(eq(5))} translates as {@code ?.? != 5}.
     */
    public static ExpressionBuilder not(ExpressionBuilder builder)
    {
        builder.getExpressions().forEach(expr -> expr.connector.negate());
        builder.getChildren().forEach(node -> not(node.getData()));
        return builder;
    }

    /**
     * Closes all expressions within the {@code builder} by wrapping it between parenthesis.
     * <p>
     * i.e. {@code closure(lt(-10).or(gt(10))} translates as {@code ( x.y < -10 or x.y > 10 )}.
     */
    public static ExpressionBuilder closure(ExpressionBuilder builder)
    {
        builder.getExpressions().addFirst(new ConnectorExpression(OPEN));
        if (builder.hasChildren())
        {
            addClose(builder.getChildren().getLast().getData());
        }
        else
        {
            builder.getExpressions().addLast(new ConnectorExpression(CLOSE));
        }
        return builder;
    }

    private static void addClose(ExpressionBuilder builder)
    {
        if (builder.hasChildren())
        {
            addClose(builder.getChildren().getLast().getData());
        }
        else
        {
            builder.getExpressions().addLast(new ConnectorExpression(CLOSE));
        }
    }

    // expressions

    /**
     * An equals expression.
     * <ul>
     * <li>i.e. {@code eq(6)} translates as {@code ?.? = 6}.
     * <li>i.e. {@code eq(null)} translates as {@code ?.? IS NULL}.
     * </ul>
     */
    public static NameBasedExpressionBuilder eq(Object value)
    {
        return new NameBasedExpressionBuilder(new Expression(EQ(), value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedExpressionBuilder like(Object value)
    {
        return new NameBasedExpressionBuilder(new Expression(LIKE(), value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static NameBasedExpressionBuilder gt(Object value)
    {
        return new NameBasedExpressionBuilder(new Expression(GT(), value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static NameBasedExpressionBuilder lt(Object value)
    {
        return new NameBasedExpressionBuilder(new Expression(LT(), value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static NameBasedExpressionBuilder in(Collection<?> values)
    {
        return new NameBasedExpressionBuilder(new Expression(IN(), Lists.newArrayList(values)));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static NameBasedExpressionBuilder between(Object v1, Object v2)
    {
        return new NameBasedExpressionBuilder(new Expression(BETWEEN(), new Object[] { v1, v2 }));
    }

    static class Connector implements Negatable
    {
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

        public String createJoinExpression(String tableName, String tableAlias, String columnName, //
                String joinTableName, String joinTableAlias, String joinColumnName)
        {
            return tableColumn(tableAlias, columnName) + " = " + tableColumn(joinTableAlias, joinColumnName);
        }

        public String createStringExpression(String lhs, ParameterBinding bindings, Object param)
        {
            return lhs + conn + bindings.createParam(param);
        }

        @Override
        public void negate()
        {
            conn = isNegated ? affirmed : negated;
            isNegated = !isNegated;
        }

        @Override
        public String toString()
        {
            return conn;
        }
    }

    static class OnlyConnector extends Connector
    {
        OnlyConnector(String affirmed, String negated)
        {
            super(affirmed, negated);
        }

        @Override
        public String createStringExpression(String lhs, ParameterBinding bindings, Object param)
        {
            return conn;
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
}
