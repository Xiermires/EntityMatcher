package org.entitymatcher;

import static org.entitymatcher.NameBasedExpressions.BETWEEN;
import static org.entitymatcher.NameBasedExpressions.CLOSE;
import static org.entitymatcher.NameBasedExpressions.EQ;
import static org.entitymatcher.NameBasedExpressions.GT;
import static org.entitymatcher.NameBasedExpressions.IN;
import static org.entitymatcher.NameBasedExpressions.LIKE;
import static org.entitymatcher.NameBasedExpressions.LT;
import static org.entitymatcher.NameBasedExpressions.OPEN;

import java.util.Collection;

import org.entitymatcher.InvokationCapturer.Capture;
import org.entitymatcher.NameBasedExpressionBuilder.ConnectorExpression;
import org.entitymatcher.NameBasedExpressionBuilder.Expression;
import org.entitymatcher.NameBasedExpressions.Negatable;

import com.google.common.collect.Lists;

public class JavaBeanBasedExpressions
{
    // matchers

    /**
     * An inner join expression.
     * <p>
     * Equivalent to ?.? = other.property
     */
    public static <T> JavaBeanBasedExpressionBuilder match(T capture)
    {
        final Capture lastCapture = JavaBeanBasedMatcher.getLastCapture();
        final Class<?> referent = JavaBeanBasedExpressionBuilder.getReferent(lastCapture);
        final String property = JavaBeanBasedExpressionBuilder.getPropertyName(lastCapture);

        final Expression expression = new Expression(EQ());
        expression.setProperty(property);
        expression.setJoinReferent(referent);
        expression.setJoinProperty(property);
        final JavaBeanBasedExpressionBuilder builder = new JavaBeanBasedExpressionBuilder(expression);
        return builder;
    }

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column
     * property.
     */
    public static <T> JavaBeanBasedExpressionBuilder match(T capture, JavaBeanBasedExpressionBuilder builder)
    {
        final Capture lastCapture = JavaBeanBasedMatcher.getLastCapture();
        final Class<?> referent = JavaBeanBasedExpressionBuilder.getReferent(lastCapture);
        final String property = JavaBeanBasedExpressionBuilder.getPropertyName(lastCapture);

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
    public static JavaBeanBasedExpressionBuilder not(JavaBeanBasedExpressionBuilder builder)
    {
        builder.getExpressions().forEach(expr -> expr.connector.negate());
        builder.getChildren().forEach(node -> NameBasedExpressions.not(node.getData()));
        return builder;
    }

    /**
     * Closes all expressions within the {@code builder} by wrapping it between parenthesis.
     * <p>
     * i.e. {@code closure(lt(-10).or(gt(10))} translates as {@code ( x.y < -10 or x.y > 10 )}.
     */
    public static JavaBeanBasedExpressionBuilder closure(JavaBeanBasedExpressionBuilder builder)
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
    public static JavaBeanBasedExpressionBuilder eq(Object value)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(EQ(), value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static JavaBeanBasedExpressionBuilder like(Object value)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(LIKE(), value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static JavaBeanBasedExpressionBuilder gt(Object value)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(GT(), value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static JavaBeanBasedExpressionBuilder lt(Object value)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(LT(), value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static JavaBeanBasedExpressionBuilder in(Collection<?> values)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(IN(), Lists.newArrayList(values)));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static JavaBeanBasedExpressionBuilder between(Object v1, Object v2)
    {
        return new JavaBeanBasedExpressionBuilder(new Expression(BETWEEN(), new Object[] { v1, v2 }));
    }
}
