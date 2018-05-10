package org.matcher.name;

import static org.matcher.name.NameBasedExpressions.selection;

import org.matcher.SelectBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.SelectExpression;

public class NameBasedSelectBuilder<T> extends SelectBuilder<T> {

    public NameBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
    }

    @Override
    protected NameBasedSelectBuilder<T> getThis() {
	return this;
    }

    public NameBasedSelectBuilder<?> and(String... properties) {
	addChild(selection(properties));
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
