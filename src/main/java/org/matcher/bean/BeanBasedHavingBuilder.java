package org.matcher.bean;

import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.OR;

import org.matcher.builder.HavingBuilder;
import org.matcher.expression.ConstantExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.OrphanExpression;
import org.matcher.expression.TypedExpression;

public class BeanBasedHavingBuilder<T> extends HavingBuilder<T> {

    public BeanBasedHavingBuilder(Class<T> leadingReferent, String leadingProperty) {
	super(leadingReferent, leadingProperty);
    }

    public BeanBasedHavingBuilder<?> or(FunctionExpression<?> function, BeanBasedWhereBuilder qualifier) {
	final TypedExpression<?> expression = createHavingExpression(function, qualifier);
	expression.getChildren().addFirst(new ConstantExpression(OR));
	getExpressions().add(expression);
	return this;
    }
    
    public BeanBasedHavingBuilder<?> and(FunctionExpression<?> function, BeanBasedWhereBuilder qualifier) {
	final TypedExpression<?> expression = createHavingExpression(function, qualifier);
	expression.getChildren().addFirst(new ConstantExpression(AND));
	getExpressions().add(expression);
	return this;
    }

    static <T> TypedExpression<?> createHavingExpression(FunctionExpression<?> function, BeanBasedWhereBuilder qualifier) {
	final TypedExpression<?> expression = new TypedExpression<>(function.getType());
	expression.addChild(function);
	for (Expression qualifierExpression : qualifier.getExpressions()) {
	    expression.addChild(new OrphanExpression(qualifierExpression));
	}
	return expression;
    }
}
