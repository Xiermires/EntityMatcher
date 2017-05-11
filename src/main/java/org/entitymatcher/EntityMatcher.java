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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.persistence.EntityManager;

public class EntityMatcher
{
    static final Map<Class<?>, Class<?>> proxy2Class = new ConcurrentHashMap<>();

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

    public static interface PreparedQuery<T>
    {
        T getSingleMatching(EntityManager em);

        List<T> getMatching(EntityManager em);
    }
}
