package org.matcher.bean;

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;
import static org.matcher.bean.BeanBasedMatcher.getReferent;

import java.util.Collection;
import java.util.List;

import org.matcher.Expressions;
import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.expression.SelectExpression;
import org.matcher.name.NameBasedExpressions;
import org.matcher.operator.Selector;

public class BeanBasedExpressions extends Expressions {

    // select / functions

    public static SelectExpression selection(Object... captures) {
	final SelectExpression leading = new SelectExpression(NONE);

	final List<Capture> lastCaptures = InvokationCapturer.getLastCaptures(captures.length);
	// revert stacked captures to maintain selection order
	for (int i = lastCaptures.size() - 1; i >= 0; i--) {
	    leading.addChild(createSelectExpression(PROPERTY(), lastCaptures.get(i)));
	}
	return leading;
    }

    public static SelectExpression selection(SelectExpression other) {
	return new SelectExpression(other);
    }

    public static <T> SelectExpression min(T capture) {
	return createSelectExpression(MIN(), capture);
    }

    public static <T> SelectExpression max(T capture) {
	return createSelectExpression(MAX(), capture);
    }

    public static <T> SelectExpression count(T capture) {
	return createSelectExpression(COUNT(), capture);
    }

    public static <T> SelectExpression distinct(T capture) {
	return createSelectExpression(DISTINCT(), capture);
    }

    private static <T> SelectExpression createSelectExpression(Selector selector, T capture) {
	return createSelectExpression(selector, BeanBasedMatcher.getLastCapture());
    }

    private static SelectExpression createSelectExpression(Selector selector, Capture capture) {
	final Class<?> referent = getReferent(capture);
	final String property = getPropertyName(capture);

	return new SelectExpression(selector, referent, property);
    }

    // matchers

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column property.
     */
    public static <T> BeanBasedExpressionBuilder matching(T capture, BeanBasedExpressionBuilder builder) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	builder.overwriteNullReferenceAndProperties(referent, property);
	return builder;
    }

    public static <T> BeanBasedExpressionBuilder matching(T capture) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	return new BeanBasedExpressionBuilder(NameBasedExpressions.matching(referent, property));
    }

    // expressions

    /**
     * An equals expression.
     * <ul>
     * <li>i.e. {@code eq(6)} translates as {@code ?.? = 6}.
     * <li>i.e. {@code eq(null)} translates as {@code ?.? IS NULL}.
     * </ul>
     */
    public static BeanBasedExpressionBuilder eq(Object value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.eq(value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static BeanBasedExpressionBuilder like(Object value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.like(value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static BeanBasedExpressionBuilder gt(Double value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedExpressionBuilder gt(Long value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedExpressionBuilder gt(Integer value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static BeanBasedExpressionBuilder lt(Double value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedExpressionBuilder lt(Long value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedExpressionBuilder lt(Integer value) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static BeanBasedExpressionBuilder in(Collection<?> values) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.in(values));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static BeanBasedExpressionBuilder between(Double v1, Double v2) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedExpressionBuilder between(Long v1, Long v2) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedExpressionBuilder between(Integer v1, Integer v2) {
	return new BeanBasedExpressionBuilder(NameBasedExpressions.between(v1, v2));
    }
}
