package org.matcher.bean;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.SelectExpression;

public class BeanBasedSelectBuilder<T> extends SelectBuilder<T, BeanBasedSelectBuilder<T>> {

    public BeanBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
    }
    
    @Override
    protected BeanBasedSelectBuilder<T> getThis() {
	return this;
    }
}
