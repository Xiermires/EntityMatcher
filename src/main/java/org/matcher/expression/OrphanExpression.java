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

import java.util.List;

import org.matcher.parameter.ParameterBinding;

/**
 * An expression w/o referent or property.
 */
public class OrphanExpression extends Expression {

    private final Expression delegate;

    public OrphanExpression(Expression delegate) {
	this.delegate = delegate;
    }

    /**
     * Resolves to an expression or to an empty string if it can't be resolved.
     */
    @Override
    public String resolve(ParameterBinding bindings) {
	return delegate.resolve(bindings);
    }

    @Override
    protected String combine(List<String> results) {
	return delegate.combine(results);
    }

    @Override
    protected String apply(String result) {
	return result;
    }

    @Override
    public String getOperator() {
	return delegate.getOperator();
    }

    @Override
    protected void setOperator(String operator) {
	delegate.setOperator(operator);
    }

    @Override
    public void setReferent(Class<?> referent) {
    }

    @Override
    public Class<?> getReferent() {
	return null;
    }

    @Override
    public void setProperty(String property) {
    }

    @Override
    public String getProperty() {
	return null;
    }

    @Override
    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
    }
}
