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

import static org.matcher.expression.Expressions.NONE;

import org.matcher.operator.Operator;

import com.google.common.base.Strings;

public class SelectExpression<T> extends Expression<Object> {

    public SelectExpression(Class<T> referent) {
	super("");
	setReferent(referent);
    }

    public SelectExpression(Class<T> referent, SelectExpression<?> expression) {
	super("");
	setReferent(referent);
	addChild(expression);
    }

    public SelectExpression(SelectExpression<T> expression) {
	this(NONE, expression);
    }

    public SelectExpression(Operator operator, SelectExpression<?> expression) {
	super(operator.getSymbol());
	setReferent(expression.getReferent());
	setProperty(expression.getProperty());
	addChild(expression);
    }

    public SelectExpression(Operator operator, String property) {
	super(operator.getSymbol());
	setProperty(property);
    }

    public SelectExpression(String operator, Class<T> referent, String property) {
	super(operator);
	setProperty(property);
	setReferent(referent);
    }

    public SelectExpression(Operator operator) {
	super(operator.getSymbol());
    }

    public SelectExpression(String property) {
	super("");
	setProperty(property);
    }

    @Override
    @SuppressWarnings("unchecked")
    // safe
    public Class<T> getReferent() {
	return (Class<T>) super.getReferent();
    }

    @Override
    protected String apply(String result) {
	return getOperator() + result;
    }

    @Override
    protected String getResolveSeparator() {
	return ", ";
    }

    @Override
    public String toString() {
	final String ref = getReferent() == null ? "?" : getReferent().getSimpleName();
	final String prop = getProperty() == null ? "?" : getProperty();
	return Strings.isNullOrEmpty(getOperator().toString()) ? ref + "." + prop : getOperator() + "(" + ref + "."
		+ prop + ")";
    }
}