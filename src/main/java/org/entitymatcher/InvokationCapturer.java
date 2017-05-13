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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class InvokationCapturer implements Observer
{
    private static final Map<Class<?>, Class<?>> proxy2Class = new ConcurrentHashMap<>();
    private final Stack<Capture> captures = new Stack<>();

    /**
     * Returns an instance of a class which might be used to match {@link Statements}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T capturer(Class<T> clazz, Predicate<Method> filter)
    {
        final ProxyFactory pf = new ProxyFactory();
        pf.setSuperclass(clazz);
        pf.setFilter(m -> filter.test(m));
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

    @SuppressWarnings("unchecked")
    protected <T> Class<T> observe(T t)
    {
        if (t instanceof ProxyObject && proxy2Class.containsKey(t.getClass()))
        {
            final MethodHandler mh = ((ProxyObject) t).getHandler();
            if (mh instanceof ObservableInvokation)
            {
                ((ObservableInvokation) mh).addObserver(this);
                return (Class<T>) proxy2Class.get(t.getClass());
            }
        }
        throw new RuntimeException("Must be instantiated with EntityMatcher#matcher()");
    }

    protected List<Class<?>> observe(Object... os)
    {
        final List<Class<?>> classes = new ArrayList<>();
        for (Object o : os)
            classes.add(observe(o));
        
        return classes;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        captures.push((Capture) arg);
    }

    protected Capture getLastCapture()
    {
        return captures.isEmpty() ? null : captures.pop();
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

    public static class Capture
    {
        Method method;

        Capture(Method method)
        {
            this.method = method;
        }
    }
}
