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
package org.matcher.bean;

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;
import static org.matcher.bean.BeanBasedMatcher.getReferent;

import java.util.Collection;
import java.util.List;

import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.expression.Expressions;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.TypedExpression;
import org.matcher.name.NameBasedExpressions;

public class BeanBasedExpressions extends Expressions {

    // select / functions

    public static BeanBasedSelectBuilder<?> selection(Object capture, Object... others) {
	final TypedExpression<?> leading = new TypedExpression<>(null);
	final List<Capture> lastCaptures = InvokationCapturer.getLastCaptures(others.length + 1);
	leading.addChild(toExpression(lastCaptures.get(lastCaptures.size() - 1)));
	// revert stacked captures to maintain selection order
	for (int i = lastCaptures.size() - 2; i >= 0; i--) {
	    leading.addChild(COMMA);
	    leading.addChild(toExpression(lastCaptures.get(i)));
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

    public static <T> FunctionExpression<?> min(T capture) {
	return createFunction(MIN, capture);
    }

    public static <T> FunctionExpression<?> max(T capture) {
	return createFunction(MAX, capture);
    }
    
    public static <T> FunctionExpression<?> avg(T capture) {
	return createFunction(AVG, capture);
    }
    
    public static <T> FunctionExpression<?> sum(T capture) {
	return createFunction(SUM, capture);
    }

    public static <T> FunctionExpression<?> count(T capture) {
	return createFunction(COUNT, capture);
    }

    public static <T> FunctionExpression<?> distinct(T capture) {
	final FunctionExpression<?> expression = createFunction(DISTINCT, capture);
	expression.setClosure(false);
	return expression;
    }

    private static TypedExpression<?> toExpression(Capture capture) {
	final TypedExpression<?> expression = new TypedExpression<>(getReferent(capture));
	expression.setProperty(getPropertyName(capture));
	return expression;
    }

    private static <T> FunctionExpression<?> createFunction(String function, T capture) {
	return createFunction(function, BeanBasedMatcher.getLastCapture());
    }

    private static FunctionExpression<?> createFunction(String function, Capture capture) {
	final FunctionExpression<?> expression = new FunctionExpression<>(function, getReferent(capture));
	expression.setProperty(getPropertyName(capture));
	return expression;
    }

    // matchers

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column property.
     */
    public static <T> BeanBasedWhereBuilder matching(T capture, BeanBasedWhereBuilder builder) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	builder.overwriteNullReferenceAndProperties(referent, property);
	return builder;
    }

    public static <T> BeanBasedWhereBuilder matching(T capture) {
	final Capture lastCapture = BeanBasedMatcher.getLastCapture();
	final Class<?> referent = getReferent(lastCapture);
	final String property = getPropertyName(lastCapture);

	return new BeanBasedWhereBuilder(NameBasedExpressions.matching(referent, property));
    }

    // expressions

    /**
     * An equals expression.
     * <ul>
     * <li>i.e. {@code eq(6)} translates as {@code ?.? = 6}.
     * <li>i.e. {@code eq(null)} translates as {@code ?.? IS NULL}.
     * </ul>
     */
    public static BeanBasedWhereBuilder eq(Object value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.eq(value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static BeanBasedWhereBuilder like(Object value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.like(value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static BeanBasedWhereBuilder gt(Double value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedWhereBuilder gt(Long value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static BeanBasedWhereBuilder gt(Integer value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.gt(value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static BeanBasedWhereBuilder lt(Double value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedWhereBuilder lt(Long value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static BeanBasedWhereBuilder lt(Integer value) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.lt(value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static BeanBasedWhereBuilder in(Collection<?> values) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.in(values));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static BeanBasedWhereBuilder between(Double v1, Double v2) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedWhereBuilder between(Long v1, Long v2) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.between(v1, v2));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static BeanBasedWhereBuilder between(Integer v1, Integer v2) {
	return new BeanBasedWhereBuilder(NameBasedExpressions.between(v1, v2));
    }
}
