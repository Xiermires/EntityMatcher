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

import org.matcher.expression.Expression;
import org.matcher.expression.JoinQualifierExpression;

public abstract class WhereBuilder<T extends WhereBuilder<T>> extends ClauseBuilder<T> {

    protected WhereBuilder(Class<?> referent, String property) {
	super(referent, property);
	setClosureOnMerge(true);
    }

    @Override
    public Set<Class<?>> getReferents() {
	final Set<Class<?>> referents = super.getReferents();
	for (Expression expression : getExpressions()) {
	    if (expression instanceof JoinQualifierExpression) {
		final JoinQualifierExpression joinExpression = (JoinQualifierExpression) expression;
		referents.add(joinExpression.getOtherReferent());
	    }
	}
	return referents;
    }

    @Override
    public ClauseType getClauseType() {
	return ClauseType.WHERE;
    }

    @Override
    protected String getPrefix() {
	return "WHERE ";
    }

    @Override
    protected String getOrOperator() {
	return OR;
    }

    @Override
    protected String getAndOperator() {
	return AND;
    }
}
