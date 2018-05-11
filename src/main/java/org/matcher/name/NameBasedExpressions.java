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

import org.matcher.builder.ExpressionBuilder;
import org.matcher.expression.Expression;
import org.matcher.expression.Expressions;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.JoinQualifierExpression;
import org.matcher.expression.QualifierExpression;
import org.matcher.expression.SelectExpression;

import com.google.common.collect.Lists;

public class NameBasedExpressions extends Expressions {

    // select

    public static <T> NameBasedSelectBuilder<T> selection(//
	    FunctionExpression<?> function, //
	    FunctionExpression<?>... others) {
	final SelectExpression<T> expression = new SelectExpression<>(NONE);
	expression.addChild(function);
	for (FunctionExpression<?> other : others) {
	    expression.addChild(other);
	}
	return new NameBasedSelectBuilder<T>(expression);
    }

    public static <T> NameBasedSelectBuilder<T> selection(String property, String... others) {
	final SelectExpression<T> expression = new SelectExpression<>(NONE);
	expression.addChild(new SelectExpression<>(PROPERTY, property));
	for (String other : others) {
	    expression.addChild(new SelectExpression<>(PROPERTY, other));
	}
	return new NameBasedSelectBuilder<T>(expression);
    }

    public static <T> NameBasedSelectBuilder<T> selection(Class<T> referent) {
	return new NameBasedSelectBuilder<T>(new SelectExpression<>(referent));
    }

    public static <T> NameBasedSelectBuilder<T> selection(Class<T> referent, String property, String... others) {
	return new NameBasedSelectBuilder<>(referent).and(selection(property, others));
    }

    public static <T> NameBasedSelectBuilder<T> selection(FunctionExpression<T> f1) {
	return new NameBasedSelectBuilder<>(new FunctionExpression<>(NONE, f1));
    }

    public static <T> NameBasedSelectBuilder<T> selection(FunctionExpression<T> f1, FunctionExpression<T> f2) {
	final FunctionExpression<T> expression = new FunctionExpression<>(NONE);
	expression.addChild(f1);
	expression.addChild(f2);
	return new NameBasedSelectBuilder<>(expression);
    }

    public static <T> NameBasedSelectBuilder<T> selection(//
	    FunctionExpression<T> f1, //
	    FunctionExpression<T> f2, //
	    FunctionExpression<T> f3) {

	final FunctionExpression<T> expression = new FunctionExpression<>(NONE);
	expression.addChild(f1);
	expression.addChild(f2);
	expression.addChild(f3);
	return new NameBasedSelectBuilder<>(expression);
    }

    @SafeVarargs
    public static NameBasedSelectBuilder<?> selection(//
	    FunctionExpression<?> f1, //
	    FunctionExpression<?> f2, //
	    FunctionExpression<?> f3, //
	    FunctionExpression<?> f4, //
	    FunctionExpression<?>... others) {

	final FunctionExpression<?> expression = new FunctionExpression<>(NONE);
	expression.addChild(f1);
	expression.addChild(f2);
	expression.addChild(f3);
	expression.addChild(f4);
	for (FunctionExpression<?> function : others) {
	    expression.addChild(function);
	}
	return new NameBasedSelectBuilder<>(expression);
    }

    public static NameBasedSelectBuilder<?> selection(Class<?> referent, FunctionExpression<?> functionExpression) {
	return new NameBasedSelectBuilder<>(new FunctionExpression<>(referent, functionExpression));
    }

    // functions

    public static FunctionExpression<?> min(String property) {
	return new FunctionExpression<>(MIN, property);
    }

    public static <T> FunctionExpression<T> min(Class<T> referent, String property) {
	return new FunctionExpression<>(MIN, referent, property);
    }

    public static FunctionExpression<?> max(String property) {
	return new FunctionExpression<>(MAX, property);
    }

    public static <T> FunctionExpression<T> max(Class<T> referent, String property) {
	return new FunctionExpression<>(MAX, referent, property);
    }

    public static FunctionExpression<?> avg(String property) {
	return new FunctionExpression<>(AVG, property);
    }

    public static <T> FunctionExpression<T> avg(Class<T> referent, String property) {
	return new FunctionExpression<>(AVG, referent, property);
    }

    public static FunctionExpression<?> sum(String property) {
	return new FunctionExpression<>(SUM, property);
    }

    public static <T> FunctionExpression<T> sum(Class<T> referent, String property) {
	return new FunctionExpression<>(SUM, referent, property);
    }

    public static FunctionExpression<?> count(String property) {
	return new FunctionExpression<>(COUNT, property);
    }

    public static <T> FunctionExpression<T> count(Class<T> referent, String property) {
	return new FunctionExpression<>(COUNT, referent, property);
    }

    public static FunctionExpression<?> count(FunctionExpression<?> other) {
	final FunctionExpression<?> count = new FunctionExpression<>(COUNT);
	count.addChild(other);
	count.setReferent(other.getReferent());
	count.setProperty(other.getProperty());
	return count;
    }

    public static FunctionExpression<?> distinct(String property) {
	return new FunctionExpression<>(DISTINCT, property);
    }

    public static <T> FunctionExpression<T> distinct(Class<T> referent, String property) {
	return new FunctionExpression<>(DISTINCT, referent, property);
    }

    // group by

    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static NameBasedAggregateBuilder<?> groupBy(String property, String... others) {
	final FunctionExpression<?> expression = new FunctionExpression<>(GROUPBY);
	expression.addChild(new SelectExpression<>(PROPERTY, property));
	for (String other : others) {
	    expression.addChild(new SelectExpression<>(PROPERTY, other));
	}
	return new NameBasedAggregateBuilder<>(expression);
    }

    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static NameBasedAggregateBuilder<?> groupBy(FunctionExpression<?> function, FunctionExpression<?>... others) {
	final FunctionExpression<?> expression = new FunctionExpression<>(GROUPBY);
	expression.addChild(function);
	for (FunctionExpression<?> other : others) {
	    expression.addChild(other);
	}
	return new NameBasedAggregateBuilder<>(expression);
    }
    
    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static <T> NameBasedAggregateBuilder<T> orderBy(String property, String... others) {
	final FunctionExpression<T> expression = new FunctionExpression<>(ORDERBY);
	expression.addChild(new SelectExpression<>(PROPERTY, property));
	for (String other : others) {
	    expression.addChild(new SelectExpression<>(PROPERTY, other));
	}
	return new NameBasedAggregateBuilder<>(expression);
    }

    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static <T> NameBasedAggregateBuilder<T> orderBy(FunctionExpression<?> function, FunctionExpression<?>... others) {
	final FunctionExpression<T> expression = new FunctionExpression<>(ORDERBY);
	expression.addChild(function);
	for (FunctionExpression<?> other : others) {
	    expression.addChild(other);
	}
	return new NameBasedAggregateBuilder<>(expression);
    }

    // matchers

    /**
     * An inner join expression.
     * <p>
     * Equivalent to ?.? = other.property
     */
    public static NameBasedFromWhereBuilder matching(Class<?> other, String otherProperty) {
	final Expression<?, ?> expression = new JoinQualifierExpression(EQ(String.class), other, otherProperty);
	final NameBasedFromWhereBuilder builder = new NameBasedFromWhereBuilder(expression);
	return matching(null, otherProperty, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a column property.
     */
    public static <T extends ExpressionBuilder<T>> T matching(String property, T builder) {
	return matching(null, property, builder);
    }

    /**
     * Typifies the {@code builder} and all its children with a referent table and a column property.
     */
    public static <T extends ExpressionBuilder<T>> T matching(Class<?> referent, String property, T builder) {
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
    public static NameBasedFromWhereBuilder eq(Object value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Object>(EQ(Object.class), value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedFromWhereBuilder like(Object value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Object>(LIKE(Object.class), value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static NameBasedFromWhereBuilder gt(Double value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Double>(GT(Double.class), value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedFromWhereBuilder gt(Long value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Long>(GT(Long.class), value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedFromWhereBuilder gt(Integer value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Integer>(GT(Integer.class), value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static NameBasedFromWhereBuilder lt(Double value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Double>(LT(Double.class), value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedFromWhereBuilder lt(Long value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Long>(LT(Long.class), value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedFromWhereBuilder lt(Integer value) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Integer>(LT(Integer.class), value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static NameBasedFromWhereBuilder in(Collection<?> values) {
	final Collection<?> newArrayList = Lists.newArrayList(values);
	return new NameBasedFromWhereBuilder(new QualifierExpression<Collection<?>>(IN(), newArrayList));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static NameBasedFromWhereBuilder between(Double v1, Double v2) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedFromWhereBuilder between(Long v1, Long v2) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedFromWhereBuilder between(Integer v1, Integer v2) {
	return new NameBasedFromWhereBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }

}