package org.matcher.expression;

import static org.matcher.expression.Expressions.NONE;

import org.matcher.operator.Selector;

public class AggregateExpression<T> extends SelectExpression<T> {

    public AggregateExpression(Selector operator) {
	super(operator);
    }
    
    public AggregateExpression(Class<T> referent) {
	super(referent);
    }

    public AggregateExpression(Class<T> referent, SelectExpression<?> expression) {
	super(NONE);
	setReferent(referent);
	addChild(expression);
    }
    
    public AggregateExpression(AggregateExpression<T> expression) {
	super(NONE);
	addChild(expression);
    }
    
    public AggregateExpression(Selector operator, Class<T> referent, String property) {
	super(operator);
	setProperty(property);
	setReferent(referent);
    }
}
