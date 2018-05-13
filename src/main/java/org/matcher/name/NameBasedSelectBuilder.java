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

import static org.matcher.name.NameBasedExpressions.*;

import org.matcher.builder.SelectBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.SelectExpression;

public class NameBasedSelectBuilder<T> extends SelectBuilder<T, NameBasedSelectBuilder<T>> {

    public NameBasedSelectBuilder(Class<T> referent) {
	super(referent);
    }

    public NameBasedSelectBuilder(SelectExpression<T> expression) {
	super(expression);
    }

    @Override
    protected NameBasedSelectBuilder<T> getThis() {
	return this;
    }

    public NameBasedSelectBuilder<?> and(String property, String... others) {
	addChild(selection(property, others));
	return this;
    }

    public NameBasedSelectBuilder<T> and(FunctionExpression<?> expression, FunctionExpression<?>... others) {
	final NameBasedSelectBuilder<T> builder = selection(expression, others);
	builder.getExpressions().addFirst(COMMA);
	addChild(builder);
	return this;
    }
}
