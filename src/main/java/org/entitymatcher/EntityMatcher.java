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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EntityMatcher
{
    static final Map<Class<?>, Class<?>> proxy2Class = new ConcurrentHashMap<Class<?>, Class<?>>();

    /**
     * Returns an instance of a class which might be used to match {@link Statements}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T matcher(Class<T> clazz)
    {
        final ProxyFactory pf = new ProxyFactory();
        pf.setSuperclass(clazz);
        pf.setFilter(m -> isBeanGetter(m));
        final Class<?> instance = pf.createClass();
        try
        {
            final T newInstance = (T) instance.newInstance();
            ((ProxyObject) newInstance).setHandler(new ObservableInvokation());
            proxy2Class.put(newInstance.getClass(), clazz);
            return newInstance;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            return null;
        }
    }

    /**
     * Returns a builder which listens to the provided classes.
     * <p>
     * The main class also determines the return method of the query.
     * <p>
     * Instances of this class are not thread safe.
     */
    public static <T> Builder<T> builder(T main, Object... others)
    {
        return new Builder<T>(main, others);
    }

    static final Pattern isGetter = Pattern.compile("(get|is)(.+)");

    private static boolean isBeanGetter(Method m)
    {
        return isGetter.matcher(m.getName()).matches();
    }

    static class ObservableInvokation extends Observable implements MethodHandler
    {
        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
        {
            setChanged();
            notifyObservers(new Capture(thisMethod));
            return proceed.invoke(self, args);
        }
    }

    static class Capture
    {
        Method method;

        public Capture(Method method)
        {
            this.method = method;
        }
    }

    static class CapturedStatement
    {
        final Capture lhs;
        final Capture rhs;
        final LhsRhsStatement<?> statement;

        public CapturedStatement(Capture lhs, Capture rhs, LhsRhsStatement<?> statement)
        {
            this.lhs = lhs;
            this.rhs = rhs;
            this.statement = statement;
        }
    }

    public static String toAlias(String table)
    {
        return table == null ? null : table.toLowerCase();
    }

    public static String toTable(String alias)
    {
        return alias == null ? null : alias.substring(0, 1).toUpperCase().concat(alias.substring(1));
    }

    public static class Builder<T> implements Observer
    {
        private Class<?> retType;
        private boolean isNativeQuery = false;
        private final Stack<Capture> captures = new Stack<Capture>();
        private final Set<String> tableNames = new LinkedHashSet<String>();
        private final ParameterBinding params = new JpqlBinding();
        private final List<Capture> customSelect = new ArrayList<Capture>();

        Builder(T main, Object... others)
        {
            assert proxy2Class.containsKey(main.getClass()) //
            : "Cannot do captures on the provided class " + main.getClass().getSimpleName()
                    + ". It must be created via EntityMatcher#matcher()";

            observe(main);
            observe(others);

            tableNames.add(EntityMatcher.proxy2Class.get(main.getClass()).getSimpleName());
            Arrays.asList(others).forEach(o -> tableNames.add(EntityMatcher.proxy2Class.get(o.getClass()).getSimpleName()));

            retType = EntityMatcher.proxy2Class.get(main.getClass());
        }

        /**
         * Overrides the specified retType T.
         * <p>
         * Use : build(instance).select(instance.getBar(),
         * instance.getFoo()).match(...).build(clazz)
         * <p>
         * Where bar and foo are the desired constants and clazz is a java bean with bar and foo
         * properties.
         */
        public Builder<T> select(Object... os)
        {
            customSelect.clear(); // doesn't allow incremental selects.
            for (int i = 0; i < os.length; i++)
            {
                final Capture lastCapture = getLastCapture();
                assert lastCapture == null : "Builders are not thread safe.";
                customSelect.add(lastCapture);
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
            final Table table = clazz.getAnnotation(Table.class);
            final String tableName = table == null || table.name().isEmpty() ? clazz.getSimpleName() : table.name();
            return tableName;
        }

        String getColumnName(Capture c)
        {
            return c == null ? null : getColumnName(c.method);
        }

        private String getColumnName(Method m)
        {
            final Matcher matcher = isGetter.matcher(m.getName());
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
            final Column c = f.getAnnotation(Column.class);
            return c == null || c.name().isEmpty() ? f.getName() : c.name();
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

        @Override
        public String toString()
        {
            return "SELECT ".concat(toAlias(retType.getSimpleName()));
        }

        class StatementComposer
        {
            private final List<CapturedStatement> statements;

            StatementComposer(CapturedStatement statement)
            {
                statements = Lists.newArrayList(statement);
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

                statements.add(new CapturedStatement(null, null, LhsStatement.and));
                statements.add(new CapturedStatement(lhs, rhs, statement));
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

                statements.add(new CapturedStatement(null, null, LhsStatement.or));
                statements.add(new CapturedStatement(lhs, rhs, statement));
                return this;
            }

            public PreparedQuery<T> build()
            {
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
                };
            }

            public <E> PreparedQuery<E> build(Class<E> clazz)
            {
                return new PreparedQuery<E>()
                {
                    @SuppressWarnings("unchecked")
                    private Function<Object[], E> copyProperties = os ->
                    {
                        try
                        {
                            assert os.length == customSelect.size() : "Request / response column size mismatch.";
                            
                            // If length == 1, it could be two options:
                            // 1. List<Integer> is = ...build(Integer.class).getMatching(em)
                            // 2. List<Foo> foos = ...build(Foo.class).getMatching(em)
                            // The first case is just a cast, the second case the class Foo has a property 
                            // named exactly as the requested column field, and we must reflect its #setName(integer).
                            
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
                            for (int i = 0; i < os.length; i++)
                            {
                                final Capture c = customSelect.get(i);
                                final Class<?> paramType = c.method.getReturnType();
                                final String setterName = c.method.getName().replaceFirst("(is|get)", "set");
                                final Method setterMethod = clazz.getMethod(setterName, paramType);
                                setterMethod.invoke(e, os[i]);
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
                    @SuppressWarnings("unchecked")
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
                };
            }

            @Override
            public String toString()
            {
                return composeStringQuery();
            }

            private String composeStringQuery()
            {
                final String select = composeSelect();

                if (statements.isEmpty())
                    return select;

                Collections.sort(statements, new Comparator<CapturedStatement>()
                {
                    @Override
                    public int compare(CapturedStatement o1, CapturedStatement o2)
                    {
                        return Boolean.compare(o1.statement.isJoinRelationship(), o2.statement.isJoinRelationship());
                    }
                });

                final Set<String> unaliasedTables = new LinkedHashSet<String>(Builder.this.tableNames);

                final StringBuilder fromClause = new StringBuilder().append(" FROM ");
                final StringBuilder whereClause = new StringBuilder().append(" WHERE ");

                final Iterator<CapturedStatement> it = statements.iterator();
                while (it.hasNext())
                {
                    final CapturedStatement next = it.next();
                    // In join relationships the main table is resolved within FROM, while joined
                    // tables are obtained from the linked properties.
                    // Example : SELECT c FROM Parent p JOIN p.children c ON p.id = c.parentid
                    if (next.statement.isJoinRelationship())
                    {
                        final String lhsTableName = getTableName(next.lhs);
                        fromClause.append(next.statement.toJpql(toAlias(lhsTableName), getColumnName(next.lhs),
                                toAlias(getTableName(next.rhs)), getColumnName(next.rhs), params)).append(", ");

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
                            fromClause.append(lhsTableName).append(" ").append(toAlias(lhsTableName)).append(", ");
                            unaliasedTables.remove(lhsTableName);
                        }
                        if (rhsTableName != null && unaliasedTables.contains(rhsTableName))
                        {
                            fromClause.append(rhsTableName).append(" ").append(toAlias(rhsTableName)).append(", ");
                            unaliasedTables.remove(rhsTableName);
                        }

                        // Add WHERE conditions
                        whereClause.append(Statement.toString(next.statement.toJpql(toAlias(lhsTableName),
                                getColumnName(next.lhs),
                                toAlias(rhsTableName), getColumnName(next.rhs), params)));
                    }
                }

                // remove last FROM unnecessary comma.
                fromClause.replace(fromClause.length() - 2, fromClause.length(), "");
                return new StringBuilder(select).append(fromClause).append(whereClause).toString();
            }

            private String composeSelect()
            {
                return !customSelect.isEmpty() ? createCustomSelect()
                        : isNativeQuery ? createNativeSelect() : "SELECT ".concat(toAlias(retType.getSimpleName()));
            }

            private String createNativeSelect()
            {
                final StringBuilder sb = new StringBuilder("SELECT ");
                final String tableName = getTableName(retType);

                // We can use reflection since calls are deterministic (even when it is not
                // guaranteed they follow the declaration order)
                for (Field f : retType.getDeclaredFields())
                {
                    if (f.isAnnotationPresent(Transient.class))
                        continue;

                    final String name = getColumnName(f);
                    sb.append(tableName).append(".").append(name).append(", ");
                }

                // remove last unnecessary comma.
                sb.replace(sb.length() - 2, sb.length(), "");
                return sb.toString();
            }

            private String createCustomSelect()
            {
                final StringBuilder sb = new StringBuilder("SELECT ");
                for (Iterator<Capture> it = customSelect.iterator(); it.hasNext();)
                {
                    final Capture next = it.next();
                    final String tableAlias = toAlias(getTableName(next));
                    final String columnName = getColumnName(next);

                    sb.append(tableAlias).append(".").append(columnName).append(it.hasNext() ? ", " : "");
                }
                return sb.toString();
            }
        }
    }

    public static interface PreparedQuery<T>
    {
        T getSingleMatching(EntityManager em);

        List<T> getMatching(EntityManager em);
    }
}
