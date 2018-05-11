package org.matcher.bean;

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;
import static org.matcher.bean.BeanBasedMatcher.getReferent;

import java.util.Collection;
import java.util.List;

import org.matcher.expression.Expressions;
import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.expression.SelectExpression;
import org.matcher.name.NameBasedExpressions;
import org.matcher.operator.Selector;

public class BeanBasedExpressions extends Expressions {

    // select / functions

    public static BeanBasedSelectBuilder<?> selection(Object... captures) {
	final SelectExpression<?> leading = new SelectExpression<>(NONE);
	final List<Capture> lastCaptures = InvokationCapturer.getLastCaptures(captures.length);
	// revert stacked captures to maintain selection order
	for (int i = lastCaptures.size() - 1; i >= 0; i--) {

	    leading.addChild(createSelectExpression(PROPERTY, lastCaptures.get(i)));
	}
	return new BeanBasedSelectBuilder<>(leading);
    }

    public static BeanBasedSelectBuilder<?> selection(Object capture, BeanBasedSelectBuilder<?> builder) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);
	builder.overwriteNullReferenceAndProperties(referent, property);
	return builder;
    }

    public static <T> BeanBasedSelectBuilder<?> min(T capture) {
	return createSelectBuilder(MIN, capture);
    }

    public static <T> BeanBasedSelectBuilder<?> max(T capture) {
	return createSelectBuilder(MAX, capture);
    }

    public static <T> BeanBasedSelectBuilder<?> count(T capture) {
	return createSelectBuilder(COUNT, capture);
    }

    public static <T> BeanBasedSelectBuilder<?> distinct(T capture) {
	return createSelectBuilder(DISTINCT, capture);
    }

    private static SelectExpression<?> createSelectExpression(Selector selector, Capture capture) {
	final Class<?> referent = getReferent(capture);
	final String property = getPropertyName(capture);

	return new SelectExpression<>(selector, referent, property);
    }

    private static <T> BeanBasedSelectBuilder<?> createSelectBuilder(Selector selector, T capture) {
	return createSelectBuilder(selector, BeanBasedMatcher.getLastCapture());
    }

    private static BeanBasedSelectBuilder<?> createSelectBuilder(Selector selector, Capture capture) {
	final Class<?> referent = getReferent(capture);
	final String property = getPropertyName(capture);

	return new BeanBasedSelectBuilder<>(new SelectExpression<>(selector, referent, property));
    }

    // matchers

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column property.
     */
    public static <T> BeanBasedFromWhereBuilder matching(T capture, BeanBasedFromWhereBuilder builder) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	builder.overwriteNullReferenceAndProperties(referent, property);
	return builder;
    }

    public static <T> BeanBasedFromWhereBuilder matching(T capture) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	return new BeanBasedFromWhereBuilder(NameBasedExpressions.matching(referent, property));
    }

    // expressions

    /**
     * An equals expression.
     * <ul>
     * <li>i.e. {@code eq(6)} translates as {@code ?.? = 6}.
     * <li>i.e. {@code eq(null)} translates as {@code ?.? IS NULL}.
     * </ul>
     */
    public static BeanBasedFromWhereBuilder eq(Object value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.eq(value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static BeanBasedFromWhereBuilder like(Object value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.like(value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static BeanBasedFromWhereBuilder gt(Double value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedFromWhereBuilder gt(Long value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedFromWhereBuilder gt(Integer value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static BeanBasedFromWhereBuilder lt(Double value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedFromWhereBuilder lt(Long value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedFromWhereBuilder lt(Integer value) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static BeanBasedFromWhereBuilder in(Collection<?> values) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.in(values));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static BeanBasedFromWhereBuilder between(Double v1, Double v2) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedFromWhereBuilder between(Long v1, Long v2) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedFromWhereBuilder between(Integer v1, Integer v2) {
	return new BeanBasedFromWhereBuilder(NameBasedExpressions.between(v1, v2));
    }
}
