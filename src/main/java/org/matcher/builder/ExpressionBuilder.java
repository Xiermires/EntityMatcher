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
import java.util.HashSet;
import java.util.Set;

import org.matcher.expression.Expression;
import org.matcher.expression.OperatorExpression;
import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public abstract class ExpressionBuilder<Builder extends ExpressionBuilder<Builder>> {

    private Class<?> leadingReferent;
    private String leadingProperty;

    private final Deque<Expression> expressions = new ArrayDeque<>();

    private boolean closureOnMerge = false;

    protected ExpressionBuilder() {
	this(null, null);
    }

    protected ExpressionBuilder(Class<?> referent) {
	this(referent, null);
    }

    protected ExpressionBuilder(Expression expression) {
	this(expression.getReferent(), expression.getProperty());
	expressions.add(expression);
    }

    protected ExpressionBuilder(Class<?> referent, String property) {
	this.leadingReferent = referent;
	this.leadingProperty = property;
    }

    protected abstract Builder getThis();

    protected void setClosureOnMerge(boolean closureOnMerge) {
	this.closureOnMerge = closureOnMerge;
    }

    protected boolean isClosureOnMerge() {
	return closureOnMerge;
    }

    public Class<?> getLeadingReferent() {
	return leadingReferent;
    }

    public String getLeadingProperty() {
	return leadingProperty;
    }

    /**
     * Gets the expressions assigned to this builder.
     */
    public Deque<Expression> getExpressions() {
	return expressions;
    }

    private Set<Class<?>> allReferents = null;

    /**
     * Gets the expressions assigned to this builder.
     */
    public Set<Class<?>> getReferents() {
	if (allReferents == null) {
	    allReferents = new HashSet<>();
	    if (getLeadingReferent() != null) {
		allReferents.add(getLeadingReferent());
	    }
	    for (Expression expression : getExpressions()) {
		if (expression.getReferent() != null) {
		    allReferents.add(expression.getReferent());
		}
	    }
	}
	return allReferents;
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public Builder or(Builder other) {
	return mergeAfterLastExpression(null, null, closureOnMerge ? closure(other) : other, getOrOperator());
    }

    protected abstract Operator getOrOperator();

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public Builder and(Builder other) {
	return mergeAfterLastExpression(null, null, closureOnMerge ? closure(other) : other, getAndOperator());
    }

    protected abstract Operator getAndOperator();

    /**
     * Builds the expression and updates the parameter bindings.
     */
    public abstract String build(Set<Class<?>> seenReferents, ParameterBinding bindings);

    protected void parseExpressions(StringBuilder appender, //
	    ParameterBinding bindings) {

	for (Expression expression : getExpressions()) {
	    if (appender != null) {
		final String resolve = expression.resolve(bindings);
		if (!resolve.isEmpty()) {
		    appender.append(resolve);
		}
	    }
	}
    }

    protected abstract String getResolveFromSeparator();

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	for (Expression expression : getExpressions()) {
	    expression.overwriteNullReferenceAndProperties(referent, property);
	}
    }

    void initializeBindings() {
	if (!getExpressions().isEmpty()) {
	    overwriteNullReferenceAndProperties(getLeadingReferent(), getLeadingProperty());
	}
    }

    protected Builder mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    Builder other, //
	    Operator operator) {

	if (operator != null) {
	    other.getExpressions().addFirst(new OperatorExpression(operator.getSymbol()));
	}
	if (referent != null) {
	    this.leadingReferent = referent;
	}
	if (property != null) {
	    this.leadingProperty = property;
	}
	getExpressions().addAll(other.getExpressions());
	return getThis();
    }
}
