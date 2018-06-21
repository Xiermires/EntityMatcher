package org.matcher.name;

import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.OR;

import org.matcher.builder.HavingBuilder;
import org.matcher.expression.ConstantExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.OrphanExpression;
import org.matcher.expression.TypedExpression;

public class NameBasedHavingBuilder<T> extends HavingBuilder<T> {

    public NameBasedHavingBuilder(Class<T> leadingReferent, String leadingProperty) {
	super(leadingReferent, leadingProperty);
    }

    public HavingBuilder<T> or(FunctionExpression<T> function, NameBasedWhereBuilder qualifier) {
	final TypedExpression<T> expression = createHavingExpression(function, qualifier);
	expression.getChildren().addFirst(new ConstantExpression(OR));
	getExpressions().add(expression);
	return this;
    }
    
    public HavingBuilder<T> and(FunctionExpression<T> function, NameBasedWhereBuilder qualifier) {
	final TypedExpression<T> expression = createHavingExpression(function, qualifier);
	expression.getChildren().addFirst(new ConstantExpression(AND));
	getExpressions().add(expression);
	return this;
    }

    static <T> TypedExpression<T> createHavingExpression(FunctionExpression<T> function, NameBasedWhereBuilder qualifier) {
	final TypedExpression<T> expression = new TypedExpression<T>(function.getType());
	expression.addChild(function);
	for (Expression qualifierExpression : qualifier.getExpressions()) {
	    expression.addChild(new OrphanExpression(qualifierExpression));
	}
	return expression;
    }
}
