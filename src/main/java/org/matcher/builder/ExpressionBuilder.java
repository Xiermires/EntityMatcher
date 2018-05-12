/*******************************************************************************
 * Copyright (c) 2018, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.matcher.builder;

import static org.matcher.expression.Expressions.closure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.matcher.expression.Expression;
import org.matcher.expression.OperatorExpression;
import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;
import org.matcher.util.Arborescence;
import org.matcher.util.ExpressionBuilderIterator;

public abstract class ExpressionBuilder<Builder extends ExpressionBuilder<Builder>> extends Arborescence<Builder> {

    private Class<?> referent;
    private String property;

    private final Deque<Expression<?>> expressions = new ArrayDeque<>();

    protected ExpressionBuilder(Class<?> referent) {
	this.referent = referent;
	this.property = null;
	setData(getThis());
    }

    protected ExpressionBuilder(Expression<?> expression) {
	expressions.add(expression);
	this.referent = expression.getReferent();
	this.property = expression.getProperty();
	setData(getThis());
    }

    protected ExpressionBuilder(Class<?> referent, String property) {
	this.referent = referent;
	this.property = property;
	setData(getThis());
    }

    protected abstract Builder getThis();

    public Class<?> getReferent() {
	return referent;
    }

    public String getProperty() {
	return property;
    }

    /**
     * Gets the expressions assigned to this builder.
     */
    public Deque<Expression<?>> getExpressions() {
	return expressions;
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public Builder or(Builder other) {
	return mergeAfterLastExpression(null, null, other.hasChildren() ? closure(other) : other, getOrOperator());
    }

    protected abstract Operator getOrOperator();

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public Builder and(Builder other) {
	return mergeAfterLastExpression(null, null, other.hasChildren() ? closure(other) : other, getAndOperator());
    }

    protected abstract Operator getAndOperator();

    /**
     * Builds the expression and updates the parameter bindings.
     */
    public abstract String build(Set<Class<?>> seenReferents, ParameterBinding bindings);

    protected void parseExpressions(StringBuilder appender, //
	    StringBuilder fromAppender, //
	    Set<Class<?>> seenReferents, //
	    ParameterBinding bindings) {

	final ExpressionBuilderIterator<Builder> it = new ExpressionBuilderIterator<>(this);
	if (it.hasNext()) {
	    Expression<?> expression = it.next();
	    if (fromAppender != null) {
		fromAppender.append(expression.resolveFromClause(seenReferents));
	    }
	    if (appender != null) {
		appender.append(expression.resolve(bindings));
	    }

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
    }

    protected abstract String getResolveFromSeparator();

    protected abstract String getResolveSeparator();

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {

	for (Expression<?> expression : getExpressions()) {
	    expression.overwriteNullReferenceAndProperties(referent, property);
	}
	getChildren().forEach(node -> node.getData().overwriteNullReferenceAndProperties(referent, property));
    }

    void initializeBindings() {
	if (hasChildren()) {
	    getChildren().forEach(c -> c.getData().initializeBindings());
	}
	if (!getExpressions().isEmpty()) {
	    overwriteNullReferenceAndProperties(getReferent(), getProperty());
	}
    }

    protected Builder mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    Builder other, //
	    Operator operator) {

	if (operator != null) {
	    other.getExpressions().addFirst(new OperatorExpression(operator));
	}
	if (referent != null) {
	    this.referent = referent;
	}
	if (property != null) {
	    this.property = property;
	}
	addChild(other);
	return getThis();
    }
}
