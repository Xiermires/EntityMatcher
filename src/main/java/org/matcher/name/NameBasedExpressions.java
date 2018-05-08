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

import org.matcher.ExpressionBuilder;
import org.matcher.Expressions;
import org.matcher.expression.Expression;
import org.matcher.expression.JoinQualifierExpression;
import org.matcher.expression.QualifierExpression;
import org.matcher.expression.SelectExpression;

import com.google.common.collect.Lists;

public class NameBasedExpressions extends Expressions {

    // select / functions
    
    public static SelectExpression selection(Class<?> referent, String... properties) {
	final SelectExpression expression = new SelectExpression(referent);
	for (String property : properties) {
	    expression.addChild(new SelectExpression(PROPERTY(), referent, property));
	}
	return expression;
    }

    public static SelectExpression selection(Class<?> referent, SelectExpression other) {
	return new SelectExpression(referent, other);
    }

    public static SelectExpression min(String property) {
	return new SelectExpression(MIN(), property);
    }
    
    public static SelectExpression min(Class<?> referent, String property) {
	return new SelectExpression(MIN(), referent, property);
    }

    public static SelectExpression max(String property) {
	return new SelectExpression(MAX(), property);
    }
    
    public static SelectExpression max(Class<?> referent, String property) {
	return new SelectExpression(MAX(), referent, property);
    }

    public static SelectExpression count(String property) {
	return new SelectExpression(COUNT(), property);
    }
    
    public static SelectExpression count(Class<?> referent, String property) {
	return new SelectExpression(COUNT(), referent, property);
    }

    public static SelectExpression count(SelectExpression otherExpression) {
	final SelectExpression count = new SelectExpression(COUNT());
	count.addChild(otherExpression);
	count.setReferent(otherExpression.getReferent());
	count.setProperty(otherExpression.getProperty());
	return count;
    }

    public static SelectExpression distinct(String property) {
	return new SelectExpression(DISTINCT(), property);
    }

    public static SelectExpression distinct(Class<?> referent, String property) {
	return new SelectExpression(DISTINCT(), referent, property);
    }
    
    // matchers

    /**
     * An inner join expression.
     * <p>
     * Equivalent to ?.? = other.property
     */
    public static NameBasedExpressionBuilder matching(Class<?> other, String otherProperty) {
	final Expression<?, ?> expression = new JoinQualifierExpression(EQ(String.class), other, otherProperty);
	final NameBasedExpressionBuilder builder = new NameBasedExpressionBuilder(expression);
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
    public static NameBasedExpressionBuilder eq(Object value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Object>(EQ(Object.class), value));
    }

    /**
     * A like expression.
     * <p>
     * i.e. {@code like("foo%")} translates as {@code ?.? LIKE 'foo%'}.
     */
    public static NameBasedExpressionBuilder like(Object value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Object>(LIKE(Object.class), value));
    }

    /**
     * A greater than expression.
     * <p>
     * i.e. {@code gt(10)} translates as {@code ?.? > 10}.
     */
    public static NameBasedExpressionBuilder gt(Double value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Double>(GT(Double.class), value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedExpressionBuilder gt(Long value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Long>(GT(Long.class), value));
    }

    /**
     * see {@link #gt(Double)}.
     */
    public static NameBasedExpressionBuilder gt(Integer value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Integer>(GT(Integer.class), value));
    }

    /**
     * A less than expression.
     * <p>
     * i.e. {@code lt(10)} translates as {@code ?.? < 10}.
     */
    public static NameBasedExpressionBuilder lt(Double value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Double>(LT(Double.class), value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedExpressionBuilder lt(Long value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Long>(LT(Long.class), value));
    }

    /**
     * see {@link #lt(Double)}.
     */
    public static NameBasedExpressionBuilder lt(Integer value) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Integer>(LT(Integer.class), value));
    }

    /**
     * An in expression.
     * <p>
     * i.e. {@code in(Arrays.asList("foo", "bar"))} translates as {@code ?.? IN ('foo', 'bar').
     */
    public static NameBasedExpressionBuilder in(Collection<?> values) {
	final Collection<?> newArrayList = Lists.newArrayList(values);
	return new NameBasedExpressionBuilder(new QualifierExpression<Collection<?>>(IN(), newArrayList));
    }

    /**
     * A between expression.
     * <p>
     * i.e. {@code between(1, 3)} translates as {@code ?.? BETWEEN 1 AND 3}.
     */
    public static NameBasedExpressionBuilder between(Double v1, Double v2) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedExpressionBuilder between(Long v1, Long v2) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }

    /**
     * see {@link #between(Double, Double)}
     */
    public static NameBasedExpressionBuilder between(Integer v1, Integer v2) {
	return new NameBasedExpressionBuilder(new QualifierExpression<Boundaries>(BETWEEN(), new Boundaries(v1, v2)));
    }
}
