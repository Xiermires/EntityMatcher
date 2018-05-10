package org.matcher;

import static org.matcher.Expressions.AND;
import static org.matcher.Expressions.OR;
import static org.matcher.Expressions.closure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
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
     * Builds the query and updates the parameter bindings.
     */
    public String build(ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder fromClause = new StringBuilder();
	final StringBuilder whereClause = new StringBuilder();
	final Set<Class<?>> seenReferents = new HashSet<>();

	parseExpressions(whereClause, fromClause, seenReferents, bindings);

	// we assume there is always a next referent, while updating the from clause. Remove the last comma.
	removeLastComma(fromClause);

	final StringBuilder sb = new StringBuilder().append("FROM ").append(fromClause);
	if (whereClause.length() > 0) {
	    sb.append(" ").append("WHERE ").append(whereClause);
	}
	return sb.toString();
    }

    protected void parseExpressions(StringBuilder whereClause, //
	    StringBuilder fromClause, //
	    Set<Class<?>> seenReferents, //
	    ParameterBinding bindings) {

	for (Expression<?, ?> expression : getExpressions()) {
	    final String resolveFromClause = expression.resolveFromClause(seenReferents);
	    if (resolveFromClause != null && !resolveFromClause.isEmpty()) {
		fromClause.append(resolveFromClause);
		fromClause.append(", ");
	    }

	    final String resolve = expression.resolve(bindings);
	    if (resolve != null && !resolve.isEmpty()) {
		whereClause.append(resolve);
		whereClause.append(" ");
	    }
	}

	if (hasChildren()) {
	    for (Node<T> child : getChildren()) {
		child.getData().parseExpressions(whereClause, fromClause, seenReferents, bindings);
	    }
	}
    }

    private StringBuilder removeLastComma(final StringBuilder sb) {
	return sb.length() == 0 ? sb : sb.replace(sb.length() - 2, sb.length(), "");
    }

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {

	for (Expression<?, ?> expression : getExpressions()) {
	    if (expression.getReferent() == null) {
		expression.setReferent(referent);
	    }
	    if (expression.getProperty() == null) {
		expression.setProperty(property);
	    }
	}
	getChildren().forEach(node -> node.getData().overwriteNullReferenceAndProperties(referent, property));
    }

    void initializeBindings() {
	if (hasChildren()) {
	    getChildren().forEach(c -> c.getData().initializeBindings());

	    final Expression<?, ?> bindingExpression = getExpressions().getFirst();
	    overwriteNullReferenceAndProperties(bindingExpression.getReferent(), bindingExpression.getProperty());
	} else {
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
