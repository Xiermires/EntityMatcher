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

import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.OR;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.matcher.expression.Expression;
import org.matcher.expression.JoinQualifierExpression;
import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public abstract class WhereBuilder<T extends WhereBuilder<T>> extends ExpressionBuilder<T> {

    final List<JoinQualifierExpression> joins = new ArrayList<>();

    protected WhereBuilder(Class<?> referent) {
	this(referent, null);
    }

    protected WhereBuilder(Expression expression) {
	super(expression);
	if (expression instanceof JoinQualifierExpression) {
	    joins.add((JoinQualifierExpression) expression);
	}
	setClosureOnMerge(true);
    }

    protected WhereBuilder(Class<?> referent, String property) {
	super(referent, property);
	setClosureOnMerge(true);
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder whereClause = new StringBuilder();
	parseExpressions(whereClause, bindings);

	final StringBuilder sb = new StringBuilder();
	if (whereClause.length() > 0) {
	    sb.append(" ").append("WHERE ").append(whereClause);
	}
	return sb.toString();
    }

    @Override
    public Set<Class<?>> getReferents() {
	final Set<Class<?>> referents = super.getReferents();
	for (JoinQualifierExpression join : joins) {
	    referents.add(join.getOtherReferent());
	}
	return referents;
    }

    @Override
    protected String getResolveFromSeparator() {
	return ", ";
    }

    @Override
    protected Operator getOrOperator() {
	return OR;
    }

    @Override
    protected Operator getAndOperator() {
	return AND;
    }

    @Override
    protected T mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    T other, //
	    Operator operator) {

	super.mergeAfterLastExpression(referent, property, other, operator);
	joins.addAll(other.joins);
	return getThis();
    }
}
