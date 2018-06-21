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

import org.matcher.expression.ConstantExpression;
import org.matcher.expression.Expression;
import org.matcher.parameter.ParameterBinding;

public abstract class ClauseBuilder<Builder extends ClauseBuilder<Builder>> {

    public static enum ClauseType {
	SELECT, FROM, WHERE, GROUP_BY, ORDER_BY, HAVING;
    }

    private Class<?> leadingReferent;
    private String leadingProperty;

    private final Deque<Expression> expressions = new ArrayDeque<>();

    private boolean closureOnMerge = false;

    private ClauseBuilder<?> previousClause;
    private ClauseBuilder<?> nextClause;

    protected ClauseBuilder(Class<?> leadingReferent, String leadingProperty) {
	this.leadingReferent = leadingReferent;
	this.leadingProperty = leadingProperty;
    }

    protected abstract Builder getThis();

    public abstract ClauseType getClauseType();

    protected void setClosureOnMerge(boolean closureOnMerge) {
	this.closureOnMerge = closureOnMerge;
    }

    protected boolean isClosureOnMerge() {
	return closureOnMerge;
    }

    public void setPreviousClause(ClauseBuilder<?> previousClause) {
	this.previousClause = previousClause;
    }

    public void setNextClause(ClauseBuilder<?> nextClause) {
	this.nextClause = nextClause;
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
    public <T extends Builder> Builder or(T other) {
	return merge(getLeadingReferent(), getLeadingProperty(), closureOnMerge ? closure(other) : other,
		getOrOperator());
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    public <T extends Builder> Builder and(T other) {
	return merge(getLeadingReferent(), getLeadingProperty(), closureOnMerge ? closure(other) : other,
		getAndOperator());
    }

    protected String getOrOperator() {
	return null;
    }

    protected String getAndOperator() {
	return null;
    }

    /**
     * Builds the expression and updates the parameter bindings.
     */
    public String build(ParameterBinding bindings) {
	final StringBuilder result = new StringBuilder();
	if (previousClause != null) {
	    result.append(build(previousClause, bindings)).append(" ");
	}
	result.append(build(this, bindings));
	if (nextClause != null) {
	    result.append(" ").append(build(nextClause, bindings));
	}
	return result.toString();
    }

    private String build(ClauseBuilder<?> builder, ParameterBinding bindings) {
	builder.initializeBindings();

	final StringBuilder appender = new StringBuilder();
	builder.parseExpressions(appender, bindings);
	if (appender.length() > 0) {
	    final StringBuilder result = new StringBuilder();
	    return result.append(builder.getPrefix()).append(appender).append(builder.getSuffix()).toString();
	}
	return "";
    }

    protected String getPrefix() {
	return "";
    }

    protected String getSuffix() {
	return "";
    }

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

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	if (referent != null || property != null) {
	    if (previousClause != null) {
		previousClause.overwriteNullReferenceAndProperties(referent, property);
	    }
	    if (nextClause != null) {
		nextClause.overwriteNullReferenceAndProperties(referent, property);
	    }
	    for (Expression expression : getExpressions()) {
		expression.overwriteNullReferenceAndProperties(referent, property);
	    }
	}
    }

    void initializeBindings() {
	if (!getExpressions().isEmpty()) {
	    overwriteNullReferenceAndProperties(getLeadingReferent(), getLeadingProperty());
	}
    }

    /**
     * Updates this clause builder leading referent and leading property and merges after this clause last expression
     * all other's expressions.
     * <p>
     * If an operator is specified, it is used to concatenate both this last and other's first expression.
     */
    public <Other extends Builder> Builder merge( //
	    Class<?> referent, //
	    String property, //
	    Other other, //
	    String operator) {

	other.overwriteNullReferenceAndProperties(referent, property);
	
	if (operator != null) {
	    getExpressions().addLast(new ConstantExpression(operator));
	}
	getExpressions().addAll(other.getExpressions());
	return getThis();
    }
}
