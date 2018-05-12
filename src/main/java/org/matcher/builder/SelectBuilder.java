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

import java.util.Set;

import org.matcher.expression.SelectExpression;
import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public abstract class SelectBuilder<T, E extends SelectBuilder<T, E>> extends ExpressionBuilder<E> {

    private final Class<T> referent;
    private final String property;

    public SelectBuilder(Class<T> referent) {
	super();
	this.referent = referent;
	this.property = null;
    }

    public SelectBuilder(SelectExpression<T> expression) {
	super();
	expressions.add(expression);
	referent = expression.getReferent();
	property = expression.getProperty();
    }

    public Class<T> getReferent() {
	return referent;
    }

    public String getProperty() {
	return property;
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder selectClause = new StringBuilder();
	parseExpressions(selectClause, null, seenReferents, bindings);
	return new StringBuilder("SELECT ").append(selectClause).toString();
    }

    @Override
    protected String getResolveFromSeparator() {
	return "";
    }

    @Override
    protected String getResolveSeparator() {
	return ", ";
    }

    @Override
    protected Operator getOrOperator() {
	throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected Operator getAndOperator() {
	return null;
    }
}
