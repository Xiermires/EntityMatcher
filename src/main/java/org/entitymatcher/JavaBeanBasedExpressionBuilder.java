package org.entitymatcher;

import static org.entitymatcher.JavaBeanBasedMatcher.camelDown;
import static org.entitymatcher.JavaBeanBasedMatcher.isGetter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;

import org.entitymatcher.InvokationCapturer.Capture;

public class JavaBeanBasedExpressionBuilder extends NameBasedExpressionBuilder
{
    public JavaBeanBasedExpressionBuilder(Expression expression)
    {
        super(expression);
    }

    @Override
    public JavaBeanBasedExpressionBuilder or(ExpressionBuilder other)
    {
        super.or(other);
        return this;
    }

    public <E> JavaBeanBasedExpressionBuilder or(E property, ExpressionBuilder other)
    {
        final Capture lastCapture = InvokationCapturer.getLastCapture();
        super.or(getPropertyName(lastCapture), other);
        return this;
    }

    @Override
    public JavaBeanBasedExpressionBuilder or(String property, ExpressionBuilder other)
    {
        final Capture lastCapture = InvokationCapturer.getLastCapture();
        super.or(getPropertyName(lastCapture), other);
        return this;
    }

    @Override
    public JavaBeanBasedExpressionBuilder and(ExpressionBuilder other)
    {
        super.and(other);
        return this;
    }

    public <E> JavaBeanBasedExpressionBuilder and(E property, ExpressionBuilder other)
    {
        final Capture lastCapture = InvokationCapturer.getLastCapture();
        super.and(getPropertyName(lastCapture), other);
        return this;
    }

    @Override
    public JavaBeanBasedExpressionBuilder and(String property, ExpressionBuilder other)
    {
        final Capture lastCapture = InvokationCapturer.getLastCapture();
        super.and(getPropertyName(lastCapture), other);
        return this;
    }

    static Class<?> getReferent(Capture c)
    {
        return c == null ? null : c.method.getDeclaringClass();
    }

    static String getPropertyName(Capture c)
    {
        return getPropertyName(getProperty(c));
    }

    private static String getPropertyName(Field f)
    {
        return f.getName();
    }

    private static Field getProperty(Capture c)
    {
        return c == null ? null : getProperty(c.method);
    }

    private static Field getProperty(Method m)
    {
        final Matcher matcher = isGetter.matcher(m.getName());
        if (matcher.matches())
        {
            final String fieldName = camelDown(matcher.group(2));
            return getField(m.getDeclaringClass(), fieldName);
        }
        throw new IllegalArgumentException("Not a getter '" + m.getName() + "'");
    }

    private static Field getField(Class<?> type, String name)
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
}
