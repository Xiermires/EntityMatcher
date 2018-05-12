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

import java.util.Set;

import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public abstract class FromWhereBuilder<T extends ExpressionBuilder<T>> extends ExpressionBuilder<T> {

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder fromClause = new StringBuilder();
	final StringBuilder whereClause = new StringBuilder();

	parseExpressions(whereClause, fromClause, seenReferents, bindings);

	final StringBuilder sb = new StringBuilder().append("FROM ").append(fromClause);
	if (whereClause.length() > 0) {
	    sb.append(" ").append("WHERE ").append(whereClause);
	}
	return sb.toString();
    }

    @Override
    protected String getResolveFromSeparator() {
	return ", ";
    }

    @Override
    protected String getResolveSeparator() {
	return " ";
    }

    @Override
    protected Operator getOrOperator() {
	return OR;
    }

    @Override
    protected Operator getAndOperator() {
	return AND;
    }
}
