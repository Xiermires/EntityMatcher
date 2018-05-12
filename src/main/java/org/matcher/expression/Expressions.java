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

import org.matcher.builder.ExpressionBuilder;
import org.matcher.builder.SelectBuilder;
import org.matcher.name.NameBasedSelectBuilder;
import org.matcher.operator.Functor;
import org.matcher.operator.Joiner;
import org.matcher.operator.Negatable;
import org.matcher.operator.NegatableOperator;
import org.matcher.operator.Operator;

public class Expressions {

    public static final Operator NONE = new Operator("");
    public static final Operator OR = new Operator(" OR ");
    public static final Operator AND = new Operator(" AND ");
    public static final Operator OPEN = new Operator(" ( ");
    public static final Operator CLOSE = new Operator(" ) ");

    // select / functions / aggregation

    public static final Operator PROPERTY = new Operator("");
    public static final Operator ORDERBY = new Operator("ORDER BY ");
    public static final Operator GROUPBY = new Operator("GROUP BY ");
    public static final Operator HAVING = new Operator("HAVING ");
    public static final Operator DISTINCT = new Operator("DISTINCT ") {
	@Override
	public String apply(String lhs, String rhs) {
	    return getSymbol() + lhs;
	}
    };

    public static final Functor MIN = new Functor("MIN");
    public static final Functor MAX = new Functor("MAX");
    public static final Functor AVG = new Functor("AVG");
    public static final Functor SUM = new Functor("SUM");
    public static final Functor COUNT = new Functor("COUNT");

    public static SelectBuilder<?, ?> count(SelectBuilder<?, ?> otherExpression) {
	final SelectExpression<?> count = new SelectExpression<>(COUNT);
	count.setReferent(otherExpression.getReferent());
	count.setProperty(otherExpression.getProperty());
	return new NameBasedSelectBuilder<>(count);
    }

    // wrappers

    /**
     * Negates an expression by changing the sign of all {@link Negatable} operators in the {@code builder}.
     * <p>
     * i.e. {@code not(eq(5))} translates as {@code ?.? != 5}.
     */
    public static <T extends ExpressionBuilder<T>> T not(T builder) {
	negate(builder);
	return builder;
    }

    private static void negate(ExpressionBuilder<?> builder) {
	for (Expression<?> expression : builder.getExpressions()) {
	    if (expression instanceof Negatable) {
		((Negatable) expression).negate();
	    }
	}
	builder.getChildren().forEach(node -> negate(node.getData()));
    }

    /**
     * Closes all expressions within the {@code builder} by wrapping it between parenthesis.
     * <p>
     * i.e. {@code closure(lt(-10).or(gt(10))} translates as {@code ( x.y < -10 or x.y > 10 )}.
     */
    public static <T extends ExpressionBuilder<T>> T closure(T builder) {
	builder.getExpressions().addFirst(new OperatorExpression(OPEN));
	addClose(builder);
	return builder;
    }

    private static void addClose(ExpressionBuilder<?> builder) {
	if (builder.hasChildren()) {
	    addClose(builder.getChildren().getLast().getData());
	} else {
	    builder.getExpressions().addLast(new OperatorExpression(CLOSE));
	}
    }

    // expressions

    public static final Joiner INNER_JOIN = new Joiner(" INNER JOIN ");

    public static final <T> NegatableOperator LIKE(Class<T> type) {
	return new NegatableOperator(" LIKE ", " NOT LIKE ");
    }

    public static final NegatableOperator EQ(Class<?> type) {
	return new NegatableOperator(" = ", " != ");
    }

    public static final <T extends Number> NegatableOperator GT(Class<? extends T> type) {
	return new NegatableOperator(" > ", " < ");
    }

    public static final <T extends Number> NegatableOperator LT(Class<T> type) {
	return new NegatableOperator(" < ", " > ");
    }

    public static final NegatableOperator IN() {
	return new NegatableOperator(" IN ", " NOT IN ");
    }

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

    public static final NegatableOperator BETWEEN = new NegatableOperator(" BETWEEN ", " NOT BETWEEN ");
}
