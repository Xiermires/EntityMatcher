package org.matcher.bean;

import org.matcher.SelectBuilder;
import org.matcher.expression.SelectExpression;

public class BeanBasedSelectBuilder<T> extends SelectBuilder<T> {

    public BeanBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
    }
    
    @Override
    protected BeanBasedSelectBuilder<T> getThis() {
	return this;
    }
}
