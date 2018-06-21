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
package org.matcher.bean;

import static org.matcher.bean.BeanBasedExpressions.selection;
import static org.matcher.expression.Expressions.COMMA;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.TypedExpression;

public class BeanBasedSelectBuilder<T> extends SelectBuilder<T, BeanBasedSelectBuilder<T>> {

    public BeanBasedSelectBuilder(TypedExpression<T> expression) {
	super(expression.getType(), expression.getProperty());
	getExpressions().add(expression);
    }

    @Override
    protected BeanBasedSelectBuilder<T> getThis() {
	return this;
    }

    public BeanBasedSelectBuilder<?> and(Object capture, Object... others) {
	getExpressions().addLast(COMMA);
	getExpressions().addAll(selection(capture, others).getExpressions());
	return this;
    }

    public BeanBasedSelectBuilder<?> and(FunctionExpression<?> expression, FunctionExpression<?>... others) {
	getExpressions().addLast(COMMA);
	getExpressions().addAll(selection(expression, others).getExpressions());
	return this;
    }
}
