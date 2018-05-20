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
package org.matcher.name;

import java.util.Collection;

import org.matcher.builder.ClauseBuilder;
import org.matcher.builder.GroupByBuilder;
import org.matcher.builder.OrderByBuilder;
import org.matcher.expression.BetweenExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.Expressions;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.JoinQualifierExpression;
import org.matcher.expression.QualifierExpression;

import com.google.common.collect.Lists;

public class NameBasedExpressions extends Expressions {

    // select

    public static <T> NameBasedSelectBuilder<T> selection(Class<T> referent, String property, String... others) {
	return new NameBasedSelectBuilder<>(referent).and(selection(property, others));
    }

    protected static <T> NameBasedSelectBuilder<T> selection(String property, String... others) {
	final NameBasedSelectBuilder<T> builder = new NameBasedSelectBuilder<>(null);
	addCommaSeparatedExpressions(builder, toExpression(property), toExpressions(others));
	return builder;
    }

    // functions

    public static <T> FunctionExpression<T> min(String property) {
	return min(null, property);
    }

    public static <T> FunctionExpression<T> min(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(MIN, referent);
	expression.setProperty(property);
	return expression;
    }

    public static <T> FunctionExpression<T> max(String property) {
	return max(null, property);
    }

    public static <T> FunctionExpression<T> max(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(MAX, referent);
	expression.setProperty(property);
	return expression;
    }

    public static <T> FunctionExpression<T> avg(String property) {
	return avg(null, property);
    }

    public static <T> FunctionExpression<T> avg(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(AVG, referent);
	expression.setProperty(property);
	return expression;
    }

    public static <T> FunctionExpression<T> sum(String property) {
	return sum(null, property);
    }

    public static <T> FunctionExpression<T> sum(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(SUM, referent);
	expression.setProperty(property);
	return expression;
    }

    public static <T> FunctionExpression<T> count(String property) {
	return count(null, property);
    }

    public static <T> FunctionExpression<T> count(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(COUNT, referent);
	expression.setProperty(property);
	return expression;
    }

    public static <T> FunctionExpression<T> distinct(String property) {
	return distinct(null, property);
    }

    public static <T> FunctionExpression<T> distinct(Class<T> referent, String property) {
	final FunctionExpression<T> expression = new FunctionExpression<>(DISTINCT, referent);
	expression.setReferent(referent);
	expression.setProperty(property);
	expression.setClosure(false);
	return expression;
    }

    // group by

    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static <T> GroupByBuilder<T> groupBy(String property, String... others) {
	final GroupByBuilder<T> builder = new GroupByBuilder<>(null, null);
	addCommaSeparatedExpressions(builder, toExpression(property), toExpressions(others));
	return builder;
    }

    // order by

    /**
     * An order by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code orderBy("foo", "bar")} translates as {@code ORDER BY ?.foo, ?.bar}.
     */
    public static <T> OrderByBuilder<T> orderBy(String property, String... others) {
	final OrderByBuilder<T> builder = new OrderByBuilder<>(null, null);
	addCommaSeparatedExpressions(builder, toExpression(property), toExpressions(others));
	return builder;
    }

    // matchers

    /**
     * An inner join expression.
     * <p>
     * Equivalent to ?.? = other.property
     */
    public static NameBasedWhereBuilder matching(Class<?> other, String otherProperty) {
	final Expression expression = new JoinQualifierExpression(EQUALS, NOT_EQUALS, other, otherProperty);
	final NameBasedWhereBuilder builder = new NameBasedWhereBuilder(expression);
	return matching(null, otherProperty, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a column property.
     */
    public static <T extends ClauseBuilder<T>> T matching(String property, T builder) {
	return matching(null, property, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column property.
     */
    public static <T extends ClauseBuilder<T>> T matching(Class<?> referent, String property, T builder) {
	builder.overwriteNullReferenceAndProperties(referent, property);
	return builder;
    }

    // expressions

    /**
     * An equals expression.
     * <ul>
     * <li>i.e. {@code eq(6)} translates as {@code ?.? = 6}.
     * <li>i.e. {@code eq(null)} translates as {@code ?.? IS NULL}.
     * </ul>
     */
    public static NameBasedWhereBuilder eq(Object value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Object>(EQUALS, NOT_EQUALS, value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedWhereBuilder like(Object value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Object>(LIKE, NOT_LIKE, value));
    }

    /**
     * Starts with syntax sugar.
     * <p>
     * i.e. {@code startsWith("foo")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedWhereBuilder startsWith(String value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Object>(LIKE, NOT_LIKE, value + "%"));
    }

    /**
     * Ends with syntax sugar.
     * <p>
     * i.e. {@code startsWith("foo")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedWhereBuilder endsWith(String value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Object>(LIKE, NOT_LIKE, "%" + value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static NameBasedWhereBuilder gt(Double value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Double>(GREATER_THAN, LESSER_THAN, value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedWhereBuilder gt(Long value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Long>(GREATER_THAN, LESSER_THAN, value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedWhereBuilder gt(Integer value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Integer>(GREATER_THAN, LESSER_THAN, value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static NameBasedWhereBuilder lt(Double value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Double>(LESSER_THAN, GREATER_THAN, value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedWhereBuilder lt(Long value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Long>(LESSER_THAN, GREATER_THAN, value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedWhereBuilder lt(Integer value) {
	return new NameBasedWhereBuilder(new QualifierExpression<Integer>(LESSER_THAN, GREATER_THAN, value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static NameBasedWhereBuilder in(Collection<?> values) {
	final Collection<?> newArrayList = Lists.newArrayList(values);
	return new NameBasedWhereBuilder(new QualifierExpression<Collection<?>>(IN, NOT_IN, newArrayList));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static NameBasedWhereBuilder between(Double v1, Double v2) {
	return new NameBasedWhereBuilder(new BetweenExpression(new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedWhereBuilder between(Long v1, Long v2) {
	return new NameBasedWhereBuilder(new BetweenExpression(new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedWhereBuilder between(Integer v1, Integer v2) {
	return new NameBasedWhereBuilder(new BetweenExpression(new Boundaries(v1, v2)));
    }

}