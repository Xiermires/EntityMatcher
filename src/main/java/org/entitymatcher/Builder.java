package org.entitymatcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.entitymatcher.EntityMatcher.Capture;
import org.entitymatcher.EntityMatcher.CapturedStatement;
import org.entitymatcher.EntityMatcher.ObservableInvokation;
import org.entitymatcher.EntityMatcher.PreparedQuery;

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;

// TODO: Cleanup
public class Builder<T> implements Observer
{
    private static enum Mode
    {
        SELECT, WHERE_COMPOSER, HAVING_COMPOSER, GROUPBY, ORDERBY
    };

    private Class<T> retType;
    private boolean isNativeQuery = false;
    private final Stack<Capture> captures = new Stack<>();
    private final ParameterBinding params = new JpqlBinding();
    private final Set<String> tableNames = new LinkedHashSet<>();
    private final ListMultimap<Mode, CapturedStatement> map = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    private Builder.ORDER_BY order = null;

    @SuppressWarnings("unchecked")
    Builder(T main, Object... others)
    {
        assert EntityMatcher.proxy2Class.containsKey(main.getClass()) //
        : "Cannot do captures on the provided class " + main.getClass().getSimpleName()
                + ". It must be created via EntityMatcher#matcher()";

        observe(main);
        observe(others);

        tableNames.add(EntityMatcher.proxy2Class.get(main.getClass()).getSimpleName());
        Arrays.asList(others).forEach(o -> tableNames.add(EntityMatcher.proxy2Class.get(o.getClass()).getSimpleName()));

        retType = (Class<T>) EntityMatcher.proxy2Class.get(main.getClass());
    }

    /**
     * Overrides the specified retType T.
     * <p>
     * Use : build(instance).select(instance.getBar(), instance.getFoo()).match(...).build(clazz)
     * <p>
     * Where bar and foo are the desired constants and clazz is a java bean with bar and foo
     * properties.
     */
    public Builder<T> select(Object... os)
    {
        map.get(Mode.SELECT).clear(); // doesn't allow incremental selects.
        for (int i = 0; i < os.length; i++)
        {
            final Capture lastCapture = getLastCapture();
            assert lastCapture == null : "Builders are not thread safe.";

            if (os[i] instanceof LhsRhsStatement) map.put(Mode.SELECT, new CapturedStatement(lastCapture, null,
                    (LhsRhsStatement<?>) os[i]));
            else map.put(Mode.SELECT, new CapturedStatement(lastCapture, null, null));
        }
        return this;
    }

    public enum ORDER_BY
    {
        ASC, DESC
    };

    public Builder<T> orderBy(Object... os)
    {
        return orderBy(ORDER_BY.ASC, os);
    }

    public Builder<T> orderBy(Builder.ORDER_BY order, Object... os)
    {
        map.get(Mode.ORDERBY).clear(); // doesn't allow incremental orderBy's.
        this.order = order;
        for (int i = 0; i < os.length; i++)
        {
            final Capture lastCapture = getLastCapture();
            assert lastCapture == null : "Builders are not thread safe.";
            map.put(Mode.ORDERBY, new CapturedStatement(lastCapture, null, null));
        }
        return this;
    }

    public Builder<T> groupBy(Object... os)
    {
        map.get(Mode.GROUPBY).clear(); // doesn't allow incremental groupBy's.
        for (int i = 0; i < os.length; i++)
        {
            final Capture lastCapture = getLastCapture();
            assert lastCapture == null : "Builders are not thread safe.";
            map.put(Mode.GROUPBY, new CapturedStatement(lastCapture, null, null));
        }
        return this;
    }

    public <E> StatementComposer match(E getter, LhsRhsStatement<? extends E> statement)
    {
        return match(statement);
    }

    public <E> StatementComposer match(LhsRhsStatement<E> statement)
    {
        final Capture lhs = getLastCapture();
        final Capture rhs = getLastCapture();

        assert lhs == null && rhs == null : "Nothing captured. Likely an invalid statement (example: 'builder().match(like(5))')";
        return new StatementComposer(new CapturedStatement(lhs, rhs, statement));
    }

    String toDescription(Capture capture)
    {
        return capture == null ? null : capture.method.getDeclaringClass().getSimpleName().toLowerCase() + "."
                + getColumnName(capture.method);
    }

    String getTableName(Capture c)
    {
        return c == null ? null : getTableName(c.method);
    }

    private String getTableName(Method m)
    {
        return getTableName(m.getDeclaringClass());
    }

    private String getTableName(Class<?> clazz)
    {
        if (isNativeQuery)
        {
            final Table table = clazz.getAnnotation(Table.class);
            final String tableName = table == null || table.name().isEmpty() ? clazz.getSimpleName() : table.name();
            return tableName;
        }
        else return clazz.getSimpleName();
    }

    String getColumnName(Capture c)
    {
        return c == null ? null : getColumnName(c.method);
    }

    private String getColumnName(Method m)
    {
        final Matcher matcher = EntityMatcher.isGetter.matcher(m.getName());
        if (matcher.matches())
        {
            final String fieldName = matcher.group(2).substring(0, 1).toLowerCase().concat(matcher.group(2).substring(1));
            return isNativeQuery ? getColumnName(getField(m.getDeclaringClass(), fieldName)) : fieldName;
        }
        throw new RuntimeException("Not a getter '" + m.getName() + "'");
    }

    private Field getField(Class<?> type, String name)
    {
        try
        {
            return type.getDeclaredField(name);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            throw new RuntimeException(type.getName() + "doesn't follow the java beans convention for field: " + name);
        }
    }

    private String getColumnName(Field f)
    {
        if (isNativeQuery)
        {
            final Column c = f.getAnnotation(Column.class);
            return c == null || c.name().isEmpty() ? f.getName() : c.name();
        }
        else return f.getName();
    }

    Capture getLastCapture()
    {
        return captures.isEmpty() ? null : captures.pop();
    }

    private void observe(Object... os)
    {
        for (Object o : os)
        {
            if (o instanceof ProxyObject)
            {
                final MethodHandler mh = ((ProxyObject) o).getHandler();
                if (mh instanceof ObservableInvokation)
                {
                    ((ObservableInvokation) mh).addObserver(this);
                    continue;
                }
            }
            throw new RuntimeException("Must be instantiated with EntityMatcher#matcher()");
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        captures.push((Capture) arg);
    }

    public Builder<T> nativeQuery(boolean b)
    {
        isNativeQuery = b;
        return this;
    }

    @Override
    public String toString()
    {
        return composeStringQuery();
    }

    public PreparedQuery<T> build()
    {
        if (isNativeQuery)
            return build(retType);

        return new PreparedQuery<T>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public T getSingleMatching(EntityManager em)
            {
                return (T) createQuery(em, isNativeQuery).getSingleResult();
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<T> getMatching(EntityManager em)
            {
                return createQuery(em, isNativeQuery).getResultList();
            }

            private Query createQuery(EntityManager em, boolean isNative)
            {
                final String queryString = composeStringQuery();
                final Query query = isNative ? em.createNativeQuery(queryString) : em.createQuery(queryString);
                params.solveQuery(queryString, query);
                return query;
            }

            @Override
            public String toString()
            {
                return composeStringQuery();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public <E> PreparedQuery<E> build(Class<E> clazz)
    {
        if (clazz.equals(retType) && !isNativeQuery)
            return (PreparedQuery<E>) build();

        return new PreparedQuery<E>()
        {
            private Function<Object[], E> copyProperties = os ->
            {
                try
                {
                    assert os.length == map.get(Mode.SELECT).size() : "Request / response column size mismatch.";

                    // If length == 1, it could be two options:
                    // 1. List<Integer> is = ...build(Integer.class).getMatching(em)
                    // 2. List<Foo> foos = ...build(Foo.class).getMatching(em)
                    // The first case is just a cast, the second case the class Foo has a
                    // property
                    // named exactly as the requested column field, and we must reflect its
                    // #setName(integer).

                    // If return is null or its type is assignable to clazz, just cast it.
                    if (os.length == 1)
                    {
                        final Object o = os[0];
                        if (o == null)
                        return null;
                        else if (clazz.isAssignableFrom(o.getClass()))
                            return (E) o; // safe
                    }

                    final E e = clazz.newInstance();
                    if (map.get(Mode.SELECT).isEmpty())
                    {
                        int i = 0;
                        // FIXME review all this.
                        for (Field f : clazz.getDeclaredFields())
                        {
                            if (f.isAnnotationPresent(Transient.class))
                                continue;

                            if (os[i] == null)
                            {
                                i++;
                                continue;
                            }

                            final int rank = NumberConversion.getRank(f.getType());
                            final Object o = rank >= 0 ? NumberConversion.convert(os[i], rank) : os[i];
                            f.set(e, o);
                            i++;
                        }
                    }
                    else
                    {
                        for (int i = 0; i < os.length; i++)
                        {
                            final CapturedStatement c = map.get(Mode.SELECT).get(i);
                            final Class<?> paramType = c.lhs.method.getReturnType();
                            final String setterName = c.lhs.method.getName().replaceFirst("(is|get)", "set");
                            final Method setterMethod = clazz.getMethod(setterName, paramType);
                            setterMethod.invoke(e, os[i]);
                        }
                    }
                    return e;
                }
                catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                        | IllegalArgumentException | InvocationTargetException e)
                {
                    assert false : "The class provided does not contain the requested named fields, is not a java bean or hasn't a default public ctor";
                }
                return null;
            };

            @Override
            public E getSingleMatching(EntityManager em)
            {
                final Object singleResult = createQuery(em, isNativeQuery).getSingleResult();
                if (singleResult != null)
                    if (singleResult.getClass().isArray())
                    return copyProperties.apply((Object[]) singleResult);
                    else
                    return copyProperties.apply(new Object[] { singleResult });

                return null;
            }

            @Override
            public List<E> getMatching(EntityManager em)
            {
                return Lists.transform(createQuery(em, isNativeQuery).getResultList(), copyProperties);
            }

            private Query createQuery(EntityManager em, boolean isNative)
            {
                final String queryString = composeStringQuery();
                final Query query = isNative ? em.createNativeQuery(queryString) : em.createQuery(queryString);
                params.solveQuery(queryString, query);
                return query;
            }

            @Override
            public String toString()
            {
                return composeStringQuery();
            }
        };
    }

    private String composeStringQuery()
    {
        final List<CapturedStatement> statements = map.get(Mode.WHERE_COMPOSER);

        Collections.sort(statements, new Comparator<CapturedStatement>()
        {
            @Override
            public int compare(CapturedStatement o1, CapturedStatement o2)
            {
                return Boolean.compare(o1.statement.isJoinRelationship(), o2.statement.isJoinRelationship());
            }
        });

        final Set<String> unaliasedTables = new LinkedHashSet<>(Builder.this.tableNames);
        final StringBuilder fromClause = new StringBuilder().append(" FROM ");
        final String select = appendSelectAndFrom(fromClause, unaliasedTables);
        if (statements.isEmpty())
        {
            return new StringBuilder(select).append(removeLastComma(fromClause)).append(getGroupByClause())
                    .append(getOrderByClause()).toString();
        }

        final StringBuilder whereClause = new StringBuilder().append(" WHERE ");
        appendFromAndWhere(unaliasedTables, fromClause, whereClause);

        final StringBuilder groupByClause = getGroupByClause();
        final StringBuilder orderByClause = getOrderByClause();

        return new StringBuilder(select).append(fromClause).append(whereClause).append(groupByClause).append(orderByClause)
                .toString();
    }

    private StringBuilder getOrderByClause()
    {
        final StringBuilder orderByClause = new StringBuilder();
        if (!map.get(Mode.ORDERBY).isEmpty())
        {
            orderByClause.append(" ORDER BY ");
            for (Iterator<CapturedStatement> it = map.get(Mode.ORDERBY).iterator(); it.hasNext();)
            {
                final CapturedStatement next = it.next();
                orderByClause.append(EntityMatcher.toAlias(getTableName(next.lhs))).append(".").append(getColumnName(next.lhs));
                if (it.hasNext())
                    orderByClause.append(", ");
            }
            orderByClause.append(" ").append(order.name());
        }
        return orderByClause;
    }

    private StringBuilder getGroupByClause()
    {
        final StringBuilder groupByClause = new StringBuilder();
        if (!map.get(Mode.GROUPBY).isEmpty())
        {
            groupByClause.append(" GROUP BY ");
            for (Iterator<CapturedStatement> it = map.get(Mode.GROUPBY).iterator(); it.hasNext();)
            {
                final CapturedStatement next = it.next();
                groupByClause.append(EntityMatcher.toAlias(getTableName(next.lhs))).append(".").append(getColumnName(next.lhs));
                if (it.hasNext())
                    groupByClause.append(", ");
            }
        }
        return groupByClause;
    }

    private void appendFromAndWhere(final Set<String> unaliasedTables, final StringBuilder fromClause,
            final StringBuilder whereClause)
    {
        final Iterator<CapturedStatement> it = map.get(Mode.WHERE_COMPOSER).iterator();
        while (it.hasNext())
        {
            final CapturedStatement next = it.next();
            // In join relationships the main table is resolved within FROM, while joined
            // tables are obtained from the linked properties.
            // Example : SELECT c FROM Parent p JOIN p.children c ON p.id = c.parentid
            if (next.statement.isJoinRelationship())
            {
                final String lhsTableName = getTableName(next.lhs);
                fromClause.append(
                        Statement.toString(next.statement.toJpql(EntityMatcher.toAlias(lhsTableName), getColumnName(next.lhs),
                                EntityMatcher.toAlias(getTableName(next.rhs)), getColumnName(next.rhs), params))).append(", ");

                // lhsTableName has been assigned an alias in the FROM clause
                unaliasedTables.remove(lhsTableName);
            }
            else
            {
                // Add aliases to the FROM clause if needed
                final String lhsTableName = getTableName(next.lhs);
                final String rhsTableName = getTableName(next.rhs);

                if (lhsTableName != null && unaliasedTables.contains(lhsTableName))
                {
                    fromClause.append(lhsTableName).append(" ").append(EntityMatcher.toAlias(lhsTableName)).append(", ");
                    unaliasedTables.remove(lhsTableName);
                }
                if (rhsTableName != null && unaliasedTables.contains(rhsTableName))
                {
                    fromClause.append(rhsTableName).append(" ").append(EntityMatcher.toAlias(rhsTableName)).append(", ");
                    unaliasedTables.remove(rhsTableName);
                }

                // Add WHERE conditions
                whereClause.append(Statement.toString(next.statement.toJpql(EntityMatcher.toAlias(lhsTableName),
                        getColumnName(next.lhs),
                        EntityMatcher.toAlias(rhsTableName), getColumnName(next.rhs), params)));
            }
        }

        removeLastComma(fromClause);
    }

    // It is actually easier to remove a known last comma, that to handle all unknowns while
    // iterating (transient, no statements, joins, ...)
    private StringBuilder removeLastComma(final StringBuilder fromClause)
    {
        return fromClause.replace(fromClause.length() - 2, fromClause.length(), "");
    }

    private String appendSelectAndFrom(StringBuilder fromClause, Set<String> unaliasedTables)
    {
        return !map.get(Mode.SELECT).isEmpty() ? createCustomSelect(fromClause, unaliasedTables)
                : isNativeQuery ? createNativeSelect(fromClause, unaliasedTables) : createSimpleSelect(fromClause,
                        unaliasedTables);
    }

    private String createSimpleSelect(StringBuilder fromClause, Set<String> unaliasedTables)
    {
        final String tableName = getTableName(retType);
        final String tableAlias = EntityMatcher.toAlias(tableName);
        unaliasedTables.remove(tableName);

        fromClause.append(tableName).append(" ").append(tableAlias).append(", ");
        return "SELECT ".concat(tableAlias);
    }

    private String createNativeSelect(StringBuilder fromClause, Set<String> unaliasedTables)
    {
        final StringBuilder sb = new StringBuilder("SELECT ");
        final String tableName = getTableName(retType);
        final String tableAlias = EntityMatcher.toAlias(tableName);

        fromClause.append(tableName).append(" ").append(tableAlias).append(", ");
        unaliasedTables.remove(tableName);

        // We can use reflection since calls are deterministic (even when it is not
        // guaranteed they follow the declaration order)
        for (Field f : retType.getDeclaredFields())
        {
            if (f.isAnnotationPresent(Transient.class))
                continue;

            final String name = getColumnName(f);
            sb.append(tableAlias).append(".").append(name).append(", ");
        }

        removeLastComma(sb);
        return sb.toString();
    }

    private String createCustomSelect(StringBuilder fromClause, Set<String> unaliasedTables)
    {
        final StringBuilder sb = new StringBuilder("SELECT ");
        for (Iterator<CapturedStatement> it = map.get(Mode.SELECT).iterator(); it.hasNext();)
        {
            final CapturedStatement next = it.next();
            final String tableName = getTableName(next.lhs);
            final String tableAlias = EntityMatcher.toAlias(tableName);
            final String columnName = getColumnName(next.lhs);

            if (unaliasedTables.contains(tableName))
            {
                fromClause.append(tableName).append(" ").append(tableAlias).append(", ");
                unaliasedTables.remove(tableName);
            }

            if (next.statement != null) sb.append(Statement.toString(next.statement.toJpql(tableAlias, columnName, null,
                    null, params)));
            else sb.append(tableAlias).append(".").append(columnName);

            sb.append(it.hasNext() ? ", " : "");
        }
        return sb.toString();
    }

    class StatementComposer
    {
        StatementComposer(CapturedStatement statement)
        {
            map.put(Mode.WHERE_COMPOSER, statement);
        }

        public StatementComposer nativeQuery(boolean b)
        {
            isNativeQuery = b;
            return this;
        }

        public <E> StatementComposer and(E getter, LhsRhsStatement<E> statement)
        {
            return and(statement);
        }

        public <E> StatementComposer and(LhsRhsStatement<E> statement)
        {
            final Capture lhs = getLastCapture();
            final Capture rhs = getLastCapture();

            assert lhs == null && rhs == null //
            : "Nothing captured. Likely an invalid statement (example: [valid -> 'and(instance.getSmth(), lt(-5).and(gt(5))']; [invalid -> 'and(instance.getSmth(), lt(-5)).and(gt(5)'])";

            map.put(Mode.WHERE_COMPOSER, new CapturedStatement(null, null, LhsStatement.and));
            map.put(Mode.WHERE_COMPOSER, new CapturedStatement(lhs, rhs, statement));
            return this;
        }

        public <E> StatementComposer or(E getter, LhsRhsStatement<E> statement)
        {
            return or(statement);
        }

        public <E> StatementComposer or(LhsRhsStatement<E> statement)
        {
            final Capture lhs = getLastCapture();
            final Capture rhs = getLastCapture();

            assert lhs == null && rhs == null //
            : "Nothing captured. Likely an invalid statement (example: [valid -> 'or(instance.getSmth(), like(\"foo\").or(gt(\"bar\"))']; [invalid -> 'or(instance.getSmth(), like(\"foo\")).or(like(\"bar\")'])";

            map.put(Mode.WHERE_COMPOSER, new CapturedStatement(null, null, LhsStatement.or));
            map.put(Mode.WHERE_COMPOSER, new CapturedStatement(lhs, rhs, statement));
            return this;
        }

        public PreparedQuery<T> build()
        {
            return Builder.this.build();
        }

        public <E> PreparedQuery<E> build(Class<E> clazz)
        {
            return Builder.this.build(clazz);
        }
    }
}