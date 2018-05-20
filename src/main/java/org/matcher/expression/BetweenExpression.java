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
package org.matcher.expression;

import static org.matcher.builder.BuilderUtils.aliasPlusColumn;
import static org.matcher.builder.BuilderUtils.getColumnName;
import static org.matcher.builder.BuilderUtils.getTableName;
import static org.matcher.builder.BuilderUtils.toAlias;
import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.BETWEEN;
import static org.matcher.expression.Expressions.NOT_BETWEEN;

import org.matcher.expression.Expressions.Boundaries;
import org.matcher.parameter.ParameterBinding;

public class BetweenExpression extends QualifierExpression<Boundaries> {

    public BetweenExpression(Boundaries value) {
	super(BETWEEN, NOT_BETWEEN, value);
    }

    @Override
    public String resolve(ParameterBinding bindings) {
	final String alias = toAlias(getTableName(getReferent()));
	final String column = getColumnName(getReferent(), getProperty());
	final String lhs = aliasPlusColumn(alias, column);
	String rhs = getOperator() + //
		bindings.createParam(getValue().min) + AND + //
		bindings.createParam(getValue().max);

	return lhs + rhs;
    }
}
