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

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;
import static org.matcher.bean.BeanBasedMatcher.getReferent;

import java.util.Deque;
import java.util.Set;

import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.builder.WhereBuilder;
import org.matcher.expression.Expression;
import org.matcher.name.NameBasedWhereBuilder;
import org.matcher.parameter.ParameterBinding;

public class BeanBasedWhereBuilder extends WhereBuilder<BeanBasedWhereBuilder> {

    private final NameBasedWhereBuilder delegate;

    public BeanBasedWhereBuilder(NameBasedWhereBuilder wrapped) {
	super(null, null);
	this.delegate = wrapped;
    }

    @Override
    protected BeanBasedWhereBuilder getThis() {
	return this;
    }

    @Override
    public Deque<Expression> getExpressions() {
	return delegate.getExpressions();
    }

    protected NameBasedWhereBuilder getDelegate() {
	return delegate;
    }

    @Override
    public Set<Class<?>> getReferents() {
	return delegate.getReferents();
    }

    public <E> BeanBasedWhereBuilder or(E property, BeanBasedWhereBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.or(getReferent(lastCapture), getPropertyName(lastCapture), other.delegate);
	return this;
    }

    public <E> BeanBasedWhereBuilder and(E property, BeanBasedWhereBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.and(getReferent(lastCapture), getPropertyName(lastCapture), other.delegate);
	return this;
    }

    @Override
    public String build(ParameterBinding bindings) {
	return delegate.build(bindings);
    }

    @Override
    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	delegate.overwriteNullReferenceAndProperties(referent, property);
    }

    @Override
    public <Other extends BeanBasedWhereBuilder> BeanBasedWhereBuilder merge(//
	    Class<?> referent, //
	    String property, //
	    Other other, //
	    String operator) {

	getDelegate().merge(referent, property, other.getDelegate(), operator);
	return getThis();
    }
}
