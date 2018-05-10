package org.matcher.expression;

import org.matcher.operator.Selector;

public class FunctionExpression<T> extends SelectExpression<T> {

    public FunctionExpression(Selector operator) {
	super(operator);
    }

    public FunctionExpression(Selector operator, FunctionExpression<?> expression) {
	super(operator, expression);
    }
    
    public FunctionExpression(Selector operator, String property) {
	super(operator);
	setProperty(property);
    }

    public FunctionExpression(Class<T> referent, FunctionExpression<?> expression) {
	super(referent, expression);
    }

    public FunctionExpression(Selector operator, Class<?> referent, String property) {
	super(operator);
	setReferent(referent);
	setProperty(property);
    }
}
