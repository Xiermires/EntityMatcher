package org.matcher;

import static org.matcher.Expressions.AND;
import static org.matcher.Expressions.OR;
import static org.matcher.Expressions.closure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

import org.matcher.expression.BindingExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.OperatorExpression;
import org.matcher.operator.Operator;

public abstract class ExpressionBuilder<T extends ExpressionBuilder<T>> extends Arborescence<T> {

    protected final Deque<Expression<?, ?>> expressions = new ArrayDeque<>();

    protected abstract T getThis();

    /**
     * Gets the expressions assigned to this builder.
     */
    public Deque<Expression<?, ?>> getExpressions() {
	return expressions;
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public T or(T other) {
	return mergeAfterLastExpression(null, null, other.hasChildren() ? closure(other) : other, OR);
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public T and(T other) {
	return mergeAfterLastExpression(null, null, other.hasChildren() ? closure(other) : other, AND);
    }

    /**
     * Builds the expression and updates the parameter bindings.
     */
    public abstract String build(Set<Class<?>> seenReferents, ParameterBinding bindings);

    protected void parseExpressions(StringBuilder appender, //
	    StringBuilder fromAppender, //
	    Set<Class<?>> seenReferents, //
	    ParameterBinding bindings) {

	final Iterator<Expression<?, ?>> it = getExpressions().iterator();
	if (it.hasNext()) {
	    Expression<?, ?> expression = it.next();
	    if (fromAppender != null)
		fromAppender.append(expression.resolveFromClause(seenReferents));

	    if (appender != null)
		appender.append(expression.resolve(bindings));

	    while (it.hasNext()) {
		expression = it.next();

		if (appender != null) {
		    final String resolve = expression.resolve(bindings);
		    if (!resolve.isEmpty()) {
			appender.append(getResolveSeparator());
			appender.append(resolve);
		    }
		}

		if (fromAppender != null) {
		    final String resolveFrom = expression.resolveFromClause(seenReferents);
		    if (!resolveFrom.isEmpty()) {
			fromAppender.append(getResolveFromSeparator());
			fromAppender.append(resolveFrom);
		    }
		}
	    }
	}

	if (hasChildren()) {
	    for (Node<T> child : getChildren()) {
		child.getData().parseExpressions(appender, fromAppender, seenReferents, bindings);
	    }
	}
    }

    protected abstract String getResolveFromSeparator();

    protected abstract String getResolveSeparator();

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {

	for (Expression<?, ?> expression : getExpressions()) {
	    expression.overwriteNullReferenceAndProperties(referent, property);
	}
	getChildren().forEach(node -> node.getData().overwriteNullReferenceAndProperties(referent, property));
    }

    void initializeBindings() {
	if (hasChildren()) {
	    getChildren().forEach(c -> c.getData().initializeBindings());

	    final Expression<?, ?> bindingExpression = getExpressions().getFirst();
	    overwriteNullReferenceAndProperties(bindingExpression.getReferent(), bindingExpression.getProperty());
	} else if (!getExpressions().isEmpty()) {
	    final Expression<?, ?> bindingExpression = getExpressions().getFirst();
	    overwriteNullReferenceAndProperties(bindingExpression.getReferent(), bindingExpression.getProperty());
	}
    }

    protected T mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    T other, //
	    Operator operator) {

	other.getExpressions().addFirst(new OperatorExpression(operator));
	other.getExpressions().addFirst(new BindingExpression(referent, property));
	addChild(other);
	return getThis();
    }
}
