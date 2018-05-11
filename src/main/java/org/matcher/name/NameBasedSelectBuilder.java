package org.matcher.name;

import static org.matcher.name.NameBasedExpressions.selection;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.SelectExpression;

public class NameBasedSelectBuilder<T> extends SelectBuilder<T, NameBasedSelectBuilder<T>> {

    public NameBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
    }

    @Override
    protected NameBasedSelectBuilder<T> getThis() {
	return this;
    }

    public NameBasedSelectBuilder<?> and(String property, String... others) {
	addChild(selection(property, others));
	return this;
    }

    public NameBasedSelectBuilder<?> and(FunctionExpression<?>... expressions) {
	for (FunctionExpression<?> expression : expressions) {
	    expression.overwriteNullReferenceAndProperties(getReferent(), getProperty());
	    getExpressions().add(expression);
	}
	return this;
    }
}
