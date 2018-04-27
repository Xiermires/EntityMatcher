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

import static org.entitymatcher.NameBasedExpressions.AND;
import static org.entitymatcher.NameBasedExpressions.NONE;
import static org.entitymatcher.NameBasedExpressions.OR;
import static org.entitymatcher.NameBasedExpressions.closure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.entitymatcher.NameBasedExpressions.Connector;

/**
 * This class allows creating expressions chaining different operators as defined in the
 * {@link NameBasedExpressions} class.
 * <p>
 * {@link NameBasedExpressionBuilder} are a tree structure which root node is typeless initially and
 * typified later on when {@link #build(Class, ParameterBinding)}.
 * <p>
 * While typifying an {@link NameBasedExpressionBuilder}, all typeless children are identically
 * typified.
 */
public class NameBasedExpressionBuilder extends Arborescence<ExpressionBuilder> implements ExpressionBuilder
{
    String property;
    Deque<Expression> expressions = new ArrayDeque<>();

    public NameBasedExpressionBuilder(Expression expression)
    {
        super();
        super.setData(this);
        expressions.add(expression);
    }

    @Override
    public Deque<Expression> getExpressions()
    {
        return expressions;
    }

    @Override
    public ExpressionBuilder or(ExpressionBuilder other)
    {
        return mergeAfterLastExpression(this, null, other.hasChildren() ? closure(other) : other, OR);
    }

    @Override
    public ExpressionBuilder or(String property, ExpressionBuilder other)
    {
        return mergeAfterLastExpression(this, property, other.hasChildren() ? closure(other) : other, OR);
    }

    @Override
    public ExpressionBuilder and(ExpressionBuilder other)
    {
        return mergeAfterLastExpression(this, null, other.hasChildren() ? closure(other) : other, AND);
    }

    @Override
    public ExpressionBuilder and(String property, ExpressionBuilder other)
    {
        return mergeAfterLastExpression(this, property, other.hasChildren() ? closure(other) : other, AND);
    }

    @Override
    public String build(Class<?> clazz, ParameterBinding params)
    {
        initializeBindings(this);
        overwriteNullReferenceAndProperties(this, clazz, null);

        final StringBuilder fromClause = new StringBuilder();
        final StringBuilder whereClause = new StringBuilder();

        parseExpressions(this, whereClause, fromClause, new HashSet<>(), params);
        removeLastComma(fromClause);

        String expressionTxt = new StringBuilder("SELECT ") //
                .append(toAlias(getTableName(clazz))) //
                .append(" FROM ") //
                .append(fromClause) //
                .append(" ") //
                .append(" WHERE ") //
                .append(whereClause).toString();

        return expressionTxt.replaceAll("\\s+", " ").trim();
    }

    static void initializeBindings(ExpressionBuilder builder)
    {
        if (builder.hasChildren())
        {
            builder.getChildren().forEach(c -> initializeBindings(c.getData()));

            final Expression bindingExpression = builder.getExpressions().getFirst();
            overwriteNullReferenceAndProperties(builder, bindingExpression.referent, bindingExpression.property);
        }
        else
        {
            final Expression bindingExpression = builder.getExpressions().getFirst();
            overwriteNullReferenceAndProperties(builder, bindingExpression.referent, bindingExpression.property);
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(" ( ");

        final Iterator<Expression> it = expressions.iterator();
        final Expression first = it.next();
        if (first instanceof BindingExpression)
        {
            sb.append(" { ");
            sb.append(first.toString());
            for (; it.hasNext();)
            {
                sb.append(it.next());
            }
            sb.append(" } ");
        }
        else
        {
            sb.append(first.toString());
            for (; it.hasNext();)
            {
                sb.append(it.next());
            }
        }

        for (Arborescence<ExpressionBuilder> node : getChildren())
        {
            final ExpressionBuilder child = node.getData();
            sb.append(child.toString());
        }
        sb.append(" ) ");
        return sb.toString().replaceAll("\\s+", " ");
    }

    static ExpressionBuilder mergeAfterLastExpression(//
            ExpressionBuilder main, //
            String property, //
            ExpressionBuilder other, //
            Connector connector)
    {

        other.getExpressions().addFirst(new ConnectorExpression(connector));
        other.getExpressions().addFirst(new BindingExpression(property));
        main.addChild(other);
        return main;
    }

    static void overwriteNullReferenceAndProperties(ExpressionBuilder builder, Class<?> referent, String property)
    {
        for (Expression expression : builder.getExpressions())
        {
            if (expression.referent == null)
            {
                expression.setReferent(referent);
            }
            if (expression.property == null)
            {
                expression.setProperty(property);
            }
        }
        builder.getChildren().forEach(node -> overwriteNullReferenceAndProperties(node.getData(), referent, property));
    }

    private static void parseExpressions(//
            ExpressionBuilder builder, //
            StringBuilder whereClause, //
            StringBuilder fromClause, //
            Set<String> seenTables, //
            ParameterBinding params)
    {
        for (Expression expression : builder.getExpressions())
        {
            final String tableName = getTableName(expression.referent);
            final String tableAlias = toAlias(tableName);
            if (tableName != null && !seenTables.contains(tableName))
            {
                fromClause.append(tableName).append(" ").append(tableAlias).append(", ");
                seenTables.add(tableName);
            }
            final String columnName = getColumnName(expression.referent, expression.property);

            if (expression.joinReferent == null)
            {
                whereClause.append(expression.connector.createStringExpression(//
                        tableColumn(tableAlias, columnName), //
                        params, //
                        expression.value));
            }
            else
            {
                final String joinTableName = getTableName(expression.joinReferent);
                final String joinTableAlias = toAlias(joinTableName);
                if (joinTableName != null && !seenTables.contains(joinTableName))
                {
                    fromClause.append(joinTableName).append(" ").append(joinTableAlias).append(", ");
                    seenTables.add(joinTableName);
                }
                final String joinColumnName = getColumnName(expression.referent, expression.property);

                whereClause.append(expression.connector.createJoinExpression(//
                        tableName, //
                        tableAlias, //
                        columnName, //
                        joinTableName, //
                        joinTableAlias, //
                        joinColumnName));
            }
        }

        builder.getChildren().forEach(node -> parseExpressions(node.getData(), whereClause, fromClause, seenTables, params));
    }

    static String toAlias(String table)
    {
        return table == null ? null : table.toLowerCase();
    }

    static String tableColumn(String table, String column)
    {
        return table == null && column == null ? null : table.concat(".").concat(column);
    }

    static String getTableName(Class<?> clazz)
    {
        return clazz != null ? clazz.getSimpleName() : null;
    }

    static String getColumnName(Class<?> clazz, String fieldName)
    {
        try
        {
            return clazz != null ? clazz.getDeclaredField(fieldName).getName() : null;
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            throw new IllegalArgumentException(//
                    "Class '" + clazz.getSimpleName() + "' doesn't contain field named '" + fieldName + "'.");
        }
    }

    // It is actually easier to remove a known last comma, that to handle all
    // unknowns while
    // iterating (transient, no statements, joins, ...)
    private StringBuilder removeLastComma(final StringBuilder sb)
    {
        return sb.length() == 0 ? sb : sb.replace(sb.length() - 2, sb.length(), "");
    }

    static class Expression
    {
        Class<?> referent;
        Class<?> joinReferent;
        String property;
        String joinProperty;
        final Connector connector;
        final Object value;

        Expression(Connector connector)
        {
            this(connector, null);
        }

        Expression(Connector connector, Object value)
        {
            this.connector = connector;
            this.value = value;
        }

        void setReferent(Class<?> referent)
        {
            this.referent = referent;
        }

        void setProperty(String property)
        {
            this.property = property;
        }

        void setJoinReferent(Class<?> joinReferent)
        {
            this.joinReferent = joinReferent;
        }

        void setJoinProperty(String joinProperty)
        {
            this.joinProperty = joinProperty;
        }

        @Override
        public String toString()
        {
            return (referent == null ? "?" : referent.getSimpleName()) + "." + (property == null ? "?" : property) + connector
                    + value;
        }
    }

    static class BindingExpression extends Expression
    {
        BindingExpression(String property)
        {
            super(NONE);
            setProperty(property);
        }

        BindingExpression(Class<?> referent, String property)
        {
            super(NONE);
            setReferent(referent);
            setProperty(property);
        }

        @Override
        public String toString()
        {
            return " bind(" + (referent == null ? "?" : referent.getSimpleName()) + "." + property + ") -> ";
        }
    }

    static class ConnectorExpression extends Expression
    {
        ConnectorExpression(Connector connector)
        {
            super(connector);
        }

        @Override
        void setReferent(Class<?> referent)
        {
        }

        @Override
        void setProperty(String property)
        {
        }

        @Override
        public String toString()
        {
            return connector.toString();
        }
    }
}
