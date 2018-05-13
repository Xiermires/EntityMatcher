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

import org.matcher.operator.Negatable;
import org.matcher.operator.NegatableOperator;
import org.matcher.parameter.ParameterBinding;

public class QualifierExpression<T> extends Expression implements Negatable {

    private final String affirmed;
    private final String negated;

    private boolean isNegated;

    private final T value;
    
    public QualifierExpression(NegatableOperator qualifier, T value) {
	super(qualifier.getSymbol());
	this.affirmed = qualifier.getAffirmed();
	this.negated = qualifier.getNegated();
	this.isNegated = qualifier.isNegated();
	this.value = value;
    }

    @Override
    public String resolve(ParameterBinding bindings) {
	final String alias = toAlias(getTableName(getReferent()));
	final String column = getColumnName(getReferent(), getProperty());
	final String lhs = aliasPlusColumn(alias, column);
	final String rhs;

	if (value == null) {
	    rhs = nullParameter();
	} else {
	    rhs = getOperator() + " " + bindings.createParam(value);
	}
	return lhs + rhs;
    }

    private String nullParameter() {
	return isNegated ? " IS NOT NULL " : " IS NULL ";
    }

    public T getValue() {
	return value;
    }
    
    @Override
    public String toString() {
	final String ref = getReferent() == null ? "?" : getReferent().getSimpleName();
	final String prop = getProperty() == null ? "?" : getProperty();
	return ref + "." + prop + getOperator() + value;
    }

    @Override
    public void negate() {
	setOperator(isNegated ? affirmed : negated);
	isNegated = !isNegated;
    }
}
