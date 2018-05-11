package org.matcher.expression;

import java.util.Collection;

import org.matcher.builder.ExpressionBuilder;
import org.matcher.builder.SelectBuilder;
import org.matcher.name.NameBasedSelectBuilder;
import org.matcher.operator.Functor;
import org.matcher.operator.Joiner;
import org.matcher.operator.Negatable;
import org.matcher.operator.Operator;
import org.matcher.operator.Qualifier;
import org.matcher.operator.Selector;
import org.matcher.parameter.ParameterBinding;

public class Expressions {

    public static final Selector NONE = new Selector();
    public static final Operator OR = new Operator(" OR ");
    public static final Operator AND = new Operator(" AND ");
    public static final Operator OPEN = new Operator(" ( ");
    public static final Operator CLOSE = new Operator(" ) ");

    // select / functions / aggregation

    public static final Selector PROPERTY = new Selector() {
	@Override
	public String resolve(String string) {
	    return string;
	}
    };

    public static final Selector ORDERBY = new Selector("ORDER BY ") {
	@Override
	public String resolve(String string) {
	    return getSymbol() + string;
	}
    };
    
    public static final Selector GROUPBY = new Selector("GROUP BY ") {
	@Override
	public String resolve(String string) {
	    return getSymbol() + string;
	}
    };

    public static final Selector DISTINCT = new Selector("DISTINCT ") {
	@Override
	public String resolve(String string) {
	    return getSymbol() + string;
	}
    };

    public static final Functor MIN = new Functor("MIN");
    public static final Functor MAX = new Functor("MAX");
    public static final Functor AVG = new Functor("AVG");
    public static final Functor SUM = new Functor("SUM");
    public static final Functor COUNT = new Functor("COUNT");

    public static SelectBuilder<?, ?> count(SelectBuilder<?,  ?> otherExpression) {
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
	for (Expression<?, ?> expression : builder.getExpressions()) {
	    if (expression.getOperator() instanceof Negatable) {
		((Negatable) expression.getOperator()).negate();
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

    public static final Joiner INNER_JOIN() {
	return new Joiner(" INNER JOIN ");
    }

    public static final <T> Qualifier<T> LIKE(Class<T> type) {
	return new Qualifier<T>(" LIKE ", " NOT LIKE ");
    }

    public static final <T> Qualifier<T> EQ(Class<T> type) {
	return new Qualifier<T>(" = ", " != ") {
	    @Override
	    public String resolve(String lhs, ParameterBinding bindings, T param) {
		if (param == null) {
		    return lhs + (isNegated() ? " IS NOT NULL " : " IS NULL ");
		} else
		    return super.resolve(lhs, bindings, param);
	    }
	};
    }

    public static final <T extends Number> Qualifier<T> GT(Class<? extends T> type) {
	return new Qualifier<T>(" > ", " < ");
    }

    public static final <T extends Number> Qualifier<T> LT(Class<T> type) {
	return new Qualifier<T>(" < ", " > ");
    }

    public static final Qualifier<Collection<?>> IN() {
	return new Qualifier<Collection<?>>(" IN ", " NOT IN ");
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

    public static final Qualifier<Boundaries> BETWEEN() {
	return new Qualifier<Boundaries>(" BETWEEN ", " NOT BETWEEN ") {
	    @Override
	    public String resolve(String lhs, ParameterBinding bindings, Boundaries param) {
		return lhs + getSymbol() + bindings.createParam(param.min) + " AND " + bindings.createParam(param.max);
	    }
	};
    };
}
