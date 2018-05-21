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
package org.matcher.expression;

import org.matcher.builder.ClauseBuilder;
import org.matcher.builder.GroupByBuilder;
import org.matcher.builder.OrderByBuilder;
import org.matcher.builder.TypedClauseBuilder;
import org.matcher.name.NameBasedSelectBuilder;

public class Expressions {

    public static final String OR = " OR ";
    public static final String AND = " AND ";
    public static final String SPACE = " ";

    public static final ConstantExpression COMMA = new ConstantExpression(", ");
    public static final ConstantExpression OPEN = new ConstantExpression(" ( ");
    public static final ConstantExpression CLOSE = new ConstantExpression(" ) ");

    // Operators

    public static final String EQUALS = " = ";
    public static final String NOT_EQUALS = " != ";
    public static final String LIKE = " LIKE ";
    public static final String NOT_LIKE = " NOT LIKE ";
    public static final String GREATER_THAN = " > ";
    public static final String LESSER_THAN = " < ";
    public static final String IN = " IN ";
    public static final String NOT_IN = " NOT IN ";
    public static final String BETWEEN = " BETWEEN ";
    public static final String NOT_BETWEEN = " NOT BETWEEN ";

    // select

    public static final String MIN = "MIN";
    public static final String MAX = "MAX";
    public static final String AVG = "AVG";
    public static final String SUM = "SUM";
    public static final String COUNT = "COUNT";
    public static final String DISTINCT = "DISTINCT";

    public static <T> NameBasedSelectBuilder<T> selection(Class<T> referent) {
	return new NameBasedSelectBuilder<T>(referent);
    }

    public static <T> NameBasedSelectBuilder<T> selection( //
	    TypedExpression<T> expression, //
	    TypedExpression<?>... others) {

	final NameBasedSelectBuilder<T> builder = new NameBasedSelectBuilder<>(expression.getType());
	addCommaSeparatedExpressions(builder, expression, others);
	return builder;
    }

    // functions

    public static <T> FunctionExpression<T> count(FunctionExpression<T> other) {
	final FunctionExpression<T> expression = new FunctionExpression<T>("COUNT", other.getType());
	expression.addChild(other);
	return expression;
    }

    // group by

    /**
     * A group by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code groupBy("foo", "bar")} translates as {@code GROUP BY ?.foo, ?.bar}.
     */
    public static <T> GroupByBuilder<T> groupBy(FunctionExpression<T> function, FunctionExpression<?>... others) {
	final GroupByBuilder<T> builder = new GroupByBuilder<>(function.getType(), null);
	addCommaSeparatedExpressions(builder, function, others);
	return builder;
    }

    // order by

    /**
     * An order by expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code orderBy("foo", "bar")} translates as {@code ORDER BY ?.foo, ?.bar}.
     */
    public static <T> OrderByBuilder<T> orderBy(FunctionExpression<T> function, FunctionExpression<?>... others) {
	final OrderByBuilder<T> builder = new OrderByBuilder<>(function.getType(), null);
	addCommaSeparatedExpressions(builder, function, others);
	return builder;
    }

    protected static Expression toExpression(String property) {
	final Expression expression = new Expression();
	expression.setProperty(property);
	return expression;
    }

    protected static Expression[] toExpressions(String... properties) {
	final Expression[] expressions = new Expression[properties.length];
	for (int i = 0; i < properties.length; i++) {
	    expressions[i] = toExpression(properties[i]);
	}
	return expressions;
    }

    protected static <T, E extends TypedClauseBuilder<T, E>> void addCommaSeparatedExpressions(E builder,
	    Expression first, Expression... others) {
	final TypedExpression<T> expression = new TypedExpression<>(builder.getType());
	expression.addChild(first);
	for (Expression other : others) {
	    expression.addChild(COMMA);
	    expression.addChild(other);
	}
	builder.getExpressions().add(expression);
    }

    // wrappers

    /**
     * Negates an expression by changing the sign of all {@link Negatable} operators in the {@code builder}.
     * <p>
     * i.e. {@code not(eq(5))} translates as {@code ?.? != 5}.
     */
    public static <T extends ClauseBuilder<T>> T not(T builder) {
	negate(builder);
	return builder;
    }

    private static void negate(ClauseBuilder<?> builder) {
	for (Expression expression : builder.getExpressions()) {
	    if (expression instanceof Negatable) {
		((Negatable) expression).negate();
	    }
	}
    }

    /**
     * Closes all expressions within the {@code builder} by wrapping it between parenthesis.
     * <p>
     * i.e. {@code closure(lt(-10).or(gt(10))} translates as {@code ( x.y < -10 or x.y > 10 )}.
     */
    public static <T extends ClauseBuilder<T>> T closure(T builder) {
	builder.getExpressions().addFirst(OPEN);
	builder.getExpressions().addLast(CLOSE);
	return builder;
    }

    // expressions

    public static class Boundaries {
	public final Number min;
	public final Number max;

	public Boundaries(Number min, Number max) {
	    this.min = min;
	    this.max = max;
	}

	@Override
	public String toString() {
	    return "( " + min + ", " + max + " )";
	}
    }
}
