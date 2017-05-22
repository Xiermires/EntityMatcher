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

import static org.entitymatcher.Builder.Mode.GROUPBY;
import static org.entitymatcher.Builder.Mode.HAVING;
import static org.entitymatcher.Builder.Mode.ORDERBY;
import static org.entitymatcher.Builder.Mode.SELECT;
import static org.entitymatcher.Builder.Mode.STATEMENTS;
import static org.entitymatcher.Builder.Order.ASC;
import static org.entitymatcher.EntityMatcher.camelDown;
import static org.entitymatcher.EntityMatcher.camelUp;
import static org.entitymatcher.EntityMatcher.isGetter;
import static org.entitymatcher.EntityMatcher.tableColumn;
import static org.entitymatcher.EntityMatcher.toAlias;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.entitymatcher.Statement.Part;

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;

public class Builder<T> extends InvokationCapturer
{
    enum Mode
    {
        SELECT, STATEMENTS, HAVING, GROUPBY, ORDERBY
    };

    public enum Order
    {
        ASC, DESC
    };

    private final Class<T> defaultRetType;
    private boolean nativeQuery = false;
    private final ParameterBinding params = new ParameterBindingImpl();
    private final Set<String> tableNames = new LinkedHashSet<>();
    private final ListMultimap<Mode, CapturedStatement> map = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    private Order order = null;

    Builder(T main, Object... others)
    {
        defaultRetType = observe(main);
        tableNames.add(defaultRetType.getSimpleName());
        observe(others).forEach(c -> tableNames.add(c.getSimpleName()));
    }

    /**
     * Overrides the specified retType T.
     * <p>
     * Use : build(instance).select(instance.getBar(), instance.getFoo()).match(...).build(clazz)
     * <p>
     * Where bar and foo are the desired constants and clazz is a java bean with bar and foo
     * properties.
     */
    public Builder<T> select(Object o, Object... os)
    {
        return processLhsStatements(SELECT, o, os);
    }

    public Builder<T> orderBy(Object... os)
    {
        return orderBy(ASC, os);
    }

    public Builder<T> orderBy(Order order, Object o, Object... os)
    {
        this.order = order;
        return processLhsStatements(ORDERBY, o, os);
    }

    public Builder<T> groupBy(Object o, Object... os)
    {
        return processLhsStatements(GROUPBY, o, os);
    }

    public Builder<T> nativeQuery(boolean b)
    {
        nativeQuery = b;
        return this;
    }

    private List<CapturedStatement> mode(Mode mode)
    {
        return map.get(mode);
    }

    public <E> LhsRhsStatementBuilder match(E getter, LhsRhsStatement<? extends E> statement)
    {
        return match(statement);
    }

    public <E> LhsRhsStatementBuilder match(LhsRhsStatement<E> statement)
    {
        final TableColumn lhs = extractTableColumn(getLastCapture());
        final TableColumn rhs = extractTableColumn(getLastCapture());
        mode(STATEMENTS).add(new CapturedStatement(lhs, rhs, statement));
        return new LhsRhsStatementBuilder();
    }

    public <E> AggregateStatementBuilder having(LhsStatement<E> aggregate, LhsStatement<E> statement)
    {
        final TableColumn lhs = extractTableColumn(getLastCapture());
        final String lhsTableName = getTableName(lhs);
        final String lhsColumnName = getColumnName(lhs);
        final String _lhsExpr = Statement.toString(aggregate.toStatement(tableColumn(toAlias(lhsTableName), lhsColumnName), null,
                params));

        final TableColumn rhs = extractTableColumn(getLastCapture());
        final String _rhsExpr = rhs == nullValue ? null : tableColumn(getTableName(rhs), getColumnName(rhs));

        final LhsStatement<T> newStatement = new LhsStatement<T>((lhsExpr, rhsExpr, params) ->
        {
            return Statement.create(Statement.toString(statement.toStatement(_lhsExpr, _rhsExpr, params)), Statement.dontNegate,
                    "");
        });

        mode(HAVING).add(new CapturedStatement(lhs, rhs, newStatement));
        return new AggregateStatementBuilder();
    }

    Builder<T> processLhsStatements(Mode mode, Object o, Object... os)
    {
        final List<CapturedStatement> capturedStatements = mode(mode);
        capturedStatements.clear();
        capturedStatements.add(captureLhsStatement(o));
        Arrays.asList(os).forEach(o_ -> capturedStatements.add(captureLhsStatement(o_)));
        return this;
    }

    CapturedStatement captureLhsStatement(Object o)
    {
        if (o instanceof LhsRhsStatement) return new CapturedStatement(extractTableColumn(getLastCapture()), null,
                (LhsRhsStatement<?>) o);
        else return new CapturedStatement(extractTableColumn(getLastCapture()), null, null);
    }

    private final TableColumn nullValue = new TableColumn(null, null);

    private TableColumn extractTableColumn(Capture capture)
    {
        if (capture == null) return nullValue;
        else return new TableColumn(getTable(capture), getColumn(capture));
    }

    private String getTableName(TableColumn tc)
    {
        return tc != null && tc.table != null ? getTableName(tc.table) : null;
    }

    private Class<?> getTable(Capture c)
    {
        return c == null ? null : c.method.getDeclaringClass();
    }

    private String getTableName(Class<?> clazz)
    {
        if (nativeQuery)
        {
            final Table table = clazz.getAnnotation(Table.class);
            final String tableName = table == null || table.name().isEmpty() ? clazz.getSimpleName() : table.name();
            return tableName;
        }
        else return clazz.getSimpleName();
    }

    private String getColumnName(TableColumn tc)
    {
        return tc != null && tc.column != null ? getColumnName(tc.column) : null;
    }

    private Class<?> getColumnType(TableColumn tc)
    {
        return tc != null && tc.column != null ? tc.column.getType() : null;
    }

    private Field getColumn(Capture c)
    {
        return c == null ? null : getColumn(c.method);
    }

    private Field getColumn(Method m)
    {
        final Matcher matcher = isGetter.matcher(m.getName());
        if (matcher.matches())
        {
            final String fieldName = camelDown(matcher.group(2));
            return getField(m.getDeclaringClass(), fieldName);
        }
        throw new IllegalArgumentException("Not a getter '" + m.getName() + "'");
    }

    private Field getField(Class<?> type, String name)
    {
        try
        {
            return type.getDeclaredField(name);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            throw new IllegalArgumentException(type.getName() + "doesn't follow the java beans convention for field: " + name);
        }
    }

    private String getColumnName(Field f)
    {
        if (nativeQuery)
        {
            final Column c = f.getAnnotation(Column.class);
            return c == null || c.name().isEmpty() ? f.getName() : c.name();
        }
        else return f.getName();
    }

    @Override
    public String toString()
    {
        return composeStringQuery();
    }

    public PreparedQuery<T> build()
    {
        return build(defaultRetType);
    }

    public <E> PreparedQuery<E> build(Class<E> clazz)
    {
        if (mode(SELECT).isEmpty())
        {
            if (nativeQuery) selectFields(clazz);
            else return createPackedSelectQuery(clazz);
        }
        return createUnpackedSelectQuery(clazz);
    }

    private void selectFields(Class<?> clazz)
    {
        final List<CapturedStatement> select = mode(SELECT);
        for (Field f : clazz.getDeclaredFields())
        {
            if (isTransient(f))
                continue;

            select.add(new CapturedStatement(new TableColumn(clazz, f), null, null));
        }
    }

    // JPA transient considered fields (final, static, transient or annotated as Transient)
    private boolean isTransient(Field f)
    {
        final int mod = f.getModifiers();
        return f.isAnnotationPresent(Transient.class) || Modifier.isFinal(mod)
                || Modifier.isStatic(mod) || Modifier.isTransient(mod);
    }

    private <E> PreparedQueryImpl<E> createPackedSelectQuery(Class<E> clazz)
    {
        return new PreparedQueryImpl<E>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public E getSingleMatching(EntityManager em)
            {
                return (E) createQuery(em, nativeQuery).getSingleResult();
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<E> getMatching(EntityManager em)
            {
                return createQuery(em, nativeQuery).getResultList();
            }
        };
    }

    private <E> PreparedQueryImpl<E> createUnpackedSelectQuery(Class<E> clazz)
    {
        return new PreparedQueryImpl<E>()
        {
            @SuppressWarnings("unchecked")
            private Function<Object[], E> copyProperties = os ->
            {
                try
                {
                    // If the value is null or its type is assignable to clazz, just cast it.
                    if (os.length == 1)
                    {
                        if (os[0] == null) return null;
                        else if (clazz.isAssignableFrom(os[0].getClass())) return (E) os[0]; // safe
                    }

                    // If multiple values or single not assignable, assign the received values in
                    // the selected order to their corresponding properties.
                    final E e = clazz.newInstance();
                    for (int i = 0; i < os.length; i++)
                    {
                        final CapturedStatement c = mode(SELECT).get(i);
                        clazz.getMethod("set".concat(camelUp(getColumnName(c.lhs))), getColumnType(c.lhs)).invoke(e, os[i]);
                    }
                    return e;
                }
                catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                        | IllegalArgumentException | InvocationTargetException e)
                {
                    throw new IllegalArgumentException(
                            "The class provided does not contain the requested named fields, is not a java bean or hasn't a default public ctor");
                }
            };

            @Override
            public E getSingleMatching(EntityManager em)
            {
                final Object singleResult = createQuery(em, nativeQuery).getSingleResult();
                if (singleResult != null)
                {
                    if (singleResult.getClass().isArray()) return copyProperties.apply((Object[]) singleResult);
                    else return copyProperties.apply(new Object[] { singleResult });
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<E> getMatching(EntityManager em)
            {
                return Lists.transform(createQuery(em, nativeQuery).getResultList(), copyProperties);
            }
        };
    }

    String composeStringQuery()
    {
        final Set<String> unaliasedTables = new LinkedHashSet<>(Builder.this.tableNames);
        final StringBuilder selectClause = new StringBuilder().append("SELECT ");
        final StringBuilder fromClause = new StringBuilder().append(" FROM ");
        processSelect(selectClause, fromClause, unaliasedTables);

        final StringBuilder whereClause = new StringBuilder();
        processStatements(whereClause, fromClause, unaliasedTables);

        final StringBuilder groupByClause = new StringBuilder();
        processGroupBy(groupByClause);

        final StringBuilder havingClause = new StringBuilder();
        processHaving(havingClause);

        final StringBuilder orderByClause = new StringBuilder();
        processOrderBy(orderByClause);

        return new StringBuilder(selectClause).append(removeLastComma(fromClause)).append(whereClause).append(groupByClause)
                .append(havingClause).append(orderByClause)
                .toString();
    }

    private void processSelect(StringBuilder selectClause, StringBuilder fromClause, Set<String> unaliasedTables)
    {
        if (mode(SELECT).isEmpty())
        {
            if (nativeQuery) appendUnpackedSelect(selectClause, fromClause, unaliasedTables);
            else appendPackedSelect(selectClause, fromClause, unaliasedTables);
        }
        else appendUnpackedSelect(selectClause, fromClause, unaliasedTables);
    }

    private void appendPackedSelect(StringBuilder selectClause, StringBuilder fromClause, Set<String> unaliasedTables)
    {
        final String tableName = getTableName(defaultRetType);
        unaliasedTables.remove(tableName);

        selectClause.append(toAlias(tableName));
        fromClause.append(tableName).append(" ").append(toAlias(tableName)).append(", ");
    }

    private void appendUnpackedSelect(StringBuilder selectClause, StringBuilder fromClause, Set<String> unaliasedTables)
    {
        for (Iterator<CapturedStatement> it = mode(SELECT).iterator(); it.hasNext();)
        {
            final CapturedStatement next = it.next();
            final String tableName = getTableName(next.lhs);
            final String tableAlias = toAlias(tableName);
            final String columnName = getColumnName(next.lhs);

            if (unaliasedTables.contains(tableName))
            {
                fromClause.append(tableName).append(" ").append(tableAlias).append(", ");
                unaliasedTables.remove(tableName);
            }

            if (next.statement != null) selectClause.append(Statement.toString(next.statement.toStatement(
                    tableColumn(tableAlias, columnName),
                    null, params)));
            else selectClause.append(tableAlias).append(".").append(columnName);

            selectClause.append(it.hasNext() ? ", " : "");
        }
    }

    private void processStatements(StringBuilder whereClause, StringBuilder fromClause, Set<String> unaliasedTables)
    {
        final List<CapturedStatement> statements = mode(STATEMENTS);
        if (!statements.isEmpty())
        {
            whereClause.append(" WHERE ");
            for (CapturedStatement captured : statements)
            {
                final LhsRhsStatement<?> lhsRhs = captured.statement;
                if (lhsRhs.isJoinRelationship())
                {
                    final String lhsTableName = getTableName(captured.lhs);
                    final List<Part> statement = lhsRhs.toStatement(
                            tableColumn(toAlias(lhsTableName), getColumnName(captured.lhs)),
                            tableColumn(toAlias(getTableName(captured.rhs)), getColumnName(captured.rhs)), params);
                    fromClause.append(Statement.toString(statement)).append(", ");
                    // lhsTableName has been assigned an alias in the FROM clause
                    unaliasedTables.remove(lhsTableName);
                }
                else
                {
                    // Add aliases to the FROM clause if needed
                    final String lhsTableName = getTableName(captured.lhs);
                    final String rhsTableName = getTableName(captured.rhs);

                    if (lhsTableName != null && unaliasedTables.contains(lhsTableName))
                    {
                        fromClause.append(lhsTableName).append(" ").append(toAlias(lhsTableName)).append(", ");
                        unaliasedTables.remove(lhsTableName);
                    }
                    if (rhsTableName != null && unaliasedTables.contains(rhsTableName))
                    {
                        fromClause.append(rhsTableName).append(" ").append(toAlias(rhsTableName)).append(", ");
                        unaliasedTables.remove(rhsTableName);
                    }

                    // Add WHERE conditions
                    final List<Part> parts = lhsRhs.toStatement(tableColumn(toAlias(lhsTableName), getColumnName(captured.lhs)),
                            tableColumn(toAlias(rhsTableName), getColumnName(captured.rhs)), params);
                    whereClause.append(Statement.toString(parts));
                }
            }
        }
    }

    private void processOrderBy(StringBuilder orderBy)
    {
        final List<CapturedStatement> captures = mode(ORDERBY);
        if (!captures.isEmpty())
        {
            orderBy.append(" ORDER BY ");
            appendLhsStatements(orderBy, captures.iterator());
            orderBy.append(" ").append(order.name());
        }
    }

    private void processHaving(StringBuilder havingClause)
    {
        final List<CapturedStatement> statements = mode(HAVING);
        if (!statements.isEmpty())
        {
            havingClause.append(" HAVING ");
            for (CapturedStatement captured : statements)
            {
                final LhsRhsStatement<?> lhsRhs = captured.statement;
                final String lhsTableName = getTableName(captured.lhs);
                final String rhsTableName = getTableName(captured.rhs);

                // Add HAVING conditions
                final List<Part> parts = lhsRhs.toStatement(tableColumn(toAlias(lhsTableName), getColumnName(captured.lhs)),
                        tableColumn(toAlias(rhsTableName), getColumnName(captured.rhs)), params);
                havingClause.append(Statement.toString(parts));
            }
        }
    }

    private void processGroupBy(StringBuilder groupBy)
    {
        final List<CapturedStatement> captures = mode(GROUPBY);
        if (!captures.isEmpty())
        {
            groupBy.append(" GROUP BY ");
            appendLhsStatements(groupBy, captures.iterator());
        }
    }

    private void appendLhsStatements(StringBuilder sb, Iterator<CapturedStatement> it)
    {
        for (; it.hasNext();)
        {
            final CapturedStatement next = it.next();
            sb.append(toAlias(getTableName(next.lhs))).append(".").append(getColumnName(next.lhs));
            if (it.hasNext())
                sb.append(", ");
        }
    }

    // It is actually easier to remove a known last comma, that to handle all unknowns while
    // iterating (transient, no statements, joins, ...)
    private StringBuilder removeLastComma(final StringBuilder sb)
    {
        return sb.length() == 0 ? sb : sb.replace(sb.length() - 2, sb.length(), "");
    }

    public static interface PreparedQuery<T>
    {
        T getSingleMatching(EntityManager em);

        List<T> getMatching(EntityManager em);
    }

    static class TableColumn
    {
        final Class<?> table;
        final Field column;

        TableColumn(Class<?> table, Field column)
        {
            this.table = table;
            this.column = column;
        }
    }

    static class CapturedStatement
    {
        final TableColumn lhs;
        final TableColumn rhs;
        final LhsRhsStatement<?> statement;

        public CapturedStatement(TableColumn lhs, TableColumn rhs, LhsRhsStatement<?> statement)
        {
            if (lhs == null && rhs == null && statement == null)
                throw new IllegalArgumentException("Nothing captured.");

            this.lhs = lhs;
            this.rhs = rhs;
            this.statement = statement;
        }
    }

    abstract class PreparedQueryImpl<E> implements PreparedQuery<E>
    {
        protected Query createQuery(EntityManager em, boolean nativeQuery)
        {
            final String queryString = composeStringQuery();
            final Query query = nativeQuery ? em.createNativeQuery(queryString) : em.createQuery(queryString);
            params.solveQuery(queryString, query);
            return query;
        }

        @Override
        public String toString()
        {
            return composeStringQuery();
        }
    }

    abstract class StatementBuilder
    {
        public PreparedQuery<T> build()
        {
            return Builder.this.build();
        }

        public <E> PreparedQuery<E> build(Class<E> clazz)
        {
            return Builder.this.build(clazz);
        }
    }

    class LhsRhsStatementBuilder extends StatementBuilder
    {
        public LhsRhsStatementBuilder nativeQuery(boolean b)
        {
            nativeQuery = b;
            return this;
        }

        public <E> LhsRhsStatementBuilder and(E getter, LhsRhsStatement<E> statement)
        {
            return and(statement);
        }

        public <E> LhsRhsStatementBuilder and(LhsRhsStatement<E> statement)
        {
            final TableColumn lhs = extractTableColumn(getLastCapture());
            final TableColumn rhs = extractTableColumn(getLastCapture());

            if (lhs == null && rhs == null)
                throw new IllegalArgumentException(
                        "Nothing captured. Likely an invalid statement (example: [valid -> 'and(instance.getSmth(), lt(-5).and(gt(5))']; [invalid -> 'and(instance.getSmth(), lt(-5)).and(gt(5)'])");

            final List<CapturedStatement> statements = mode(STATEMENTS);
            statements.add(new CapturedStatement(null, null, LhsStatement.and));
            statements.add(new CapturedStatement(lhs, rhs, statement));
            return this;
        }

        public <E> LhsRhsStatementBuilder or(E getter, LhsRhsStatement<E> statement)
        {
            return or(statement);
        }

        public <E> LhsRhsStatementBuilder or(LhsRhsStatement<E> statement)
        {
            final TableColumn lhs = extractTableColumn(getLastCapture());
            final TableColumn rhs = extractTableColumn(getLastCapture());

            if (lhs == null && rhs == null)
                throw new IllegalArgumentException(
                        "Nothing captured. Likely an invalid statement (example: [valid -> 'or(instance.getSmth(), like(\"foo\").or(gt(\"bar\"))']; [invalid -> 'or(instance.getSmth(), like(\"foo\")).or(like(\"bar\")'])");

            final List<CapturedStatement> statements = mode(STATEMENTS);
            statements.add(new CapturedStatement(null, null, LhsStatement.or));
            statements.add(new CapturedStatement(lhs, rhs, statement));
            return this;
        }
    }

    class AggregateStatementBuilder extends StatementBuilder
    {
        public AggregateStatementBuilder nativeQuery(boolean b)
        {
            nativeQuery = b;
            return this;
        }

        public <E> AggregateStatementBuilder and(LhsStatement<E> aggregate, LhsRhsStatement<E> statement)
        {
            final TableColumn lhs = extractTableColumn(getLastCapture());
            final TableColumn rhs = extractTableColumn(getLastCapture());

            if (lhs == null && rhs == null)
                throw new IllegalArgumentException(
                        "Nothing captured. Likely an invalid statement (example: [valid -> 'having(aggreagate(instance.getSmth()), lt(-5).and(aggregate(instance.getSmth(), gt(5))'];");

            final List<CapturedStatement> statements = mode(HAVING);
            statements.add(new CapturedStatement(null, null, LhsStatement.and));
            statements.add(new CapturedStatement(lhs, rhs, statement));
            return this;
        }

        public <E> AggregateStatementBuilder or(LhsStatement<E> aggregate, LhsRhsStatement<E> statement)
        {
            final TableColumn lhs = extractTableColumn(getLastCapture());
            final TableColumn rhs = extractTableColumn(getLastCapture());

            if (lhs == null && rhs == null)
                throw new IllegalArgumentException(
                        "Nothing captured. Likely an invalid statement (example: [valid -> 'having(aggreagate(instance.getSmth()), lt(-5).or(aggregate(instance.getSmth(), gt(5))'];");

            final List<CapturedStatement> statements = mode(HAVING);
            statements.add(new CapturedStatement(null, null, LhsStatement.or));
            statements.add(new CapturedStatement(lhs, rhs, statement));
            return this;
        }
    }
}