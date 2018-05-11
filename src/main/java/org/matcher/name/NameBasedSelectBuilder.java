package org.matcher.name;

import static org.matcher.name.NameBasedExpressions.selection;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.SelectExpression;

public class NameBasedSelectBuilder<T> extends SelectBuilder<T, NameBasedSelectBuilder<T>> {

    public NameBasedSelectBuilder(Class<T> referent) {
	super(referent);
	setData(this);
    }

    public NameBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
	setData(this);
    }

    @Override
    protected NameBasedSelectBuilder<T> getThis() {
	return this;
    }

    public NameBasedSelectBuilder<?> and(String property, String... others) {
	addChild(selection(property, others));
	return this;
    }

    public NameBasedSelectBuilder<T> and(FunctionExpression<?> expression, FunctionExpression<?>... others) {
	addChild(selection(expression, others));
	return this;
    }
}
