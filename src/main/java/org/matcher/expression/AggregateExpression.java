package org.matcher.expression;

import static org.matcher.Expressions.NONE;

import org.matcher.operator.Selector;

public class AggregatorExpression<T> extends SelectExpression<T> {

    public AggregatorExpression(Selector operator) {
	super(operator);
    }
    
    public AggregatorExpression(Class<T> referent) {
	super(referent);
    }

    public AggregatorExpression(Class<T> referent, SelectExpression<?> expression) {
	super(NONE);
	setReferent(referent);
	addChild(expression);
    }
    
    public AggregatorExpression(AggregatorExpression<T> expression) {
	super(NONE);
	addChild(expression);
    }
    
    public AggregatorExpression(Selector operator, Class<T> referent, String property) {
	super(operator);
	setProperty(property);
	setReferent(referent);
    }
}
