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
package org.matcher.name;

import static org.matcher.expression.Expressions.COMMA;
import static org.matcher.name.NameBasedExpressions.selection;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.FunctionExpression;

public class NameBasedSelectBuilder<T> extends SelectBuilder<T, NameBasedSelectBuilder<T>> {

    public NameBasedSelectBuilder(Class<T> type) {
	super(type, null);
    }

    @Override
    protected NameBasedSelectBuilder<T> getThis() {
	return this;
    }

    public NameBasedSelectBuilder<?> and(String property, String... others) {
	getExpressions().addLast(COMMA);
	getExpressions().addAll(selection(property, others).getExpressions());
	return this;
    }

    public NameBasedSelectBuilder<T> and(FunctionExpression<T> expression, FunctionExpression<?>... others) {
	getExpressions().addLast(COMMA);
	getExpressions().addAll(selection(expression, others).getExpressions());
	return this;
    }
}
