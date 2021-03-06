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

import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.OR;
import static org.matcher.expression.Expressions.closure;
import static org.matcher.name.NameBasedExpressions.matching;

import java.util.Iterator;

import org.matcher.builder.WhereBuilder;
import org.matcher.expression.Expression;
import org.matcher.parameter.ParameterBinding;

/**
 * This class allows creating expressions chaining different operators as defined in the {@link NameBasedExpressions}
 * class.
 * <p>
 * {@link NameBasedWhereBuilder} are a tree structure which root node is typeless initially and typified later on when
 * {@link #build(Class, ParameterBinding)}.
 * <p>
 * While typifying an {@link NameBasedWhereBuilder}, all typeless children are identically typified.
 */
public class NameBasedWhereBuilder extends WhereBuilder<NameBasedWhereBuilder> {

    public NameBasedWhereBuilder(Class<?> referent, String property) {
	super(referent, property);
    }

    // syntax sugar
    NameBasedWhereBuilder(Expression expression) {
	super(expression.getReferent(), expression.getProperty());
	getExpressions().add(expression);
    }

    @Override
    public NameBasedWhereBuilder getThis() {
	return this;
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined in the this
     * builder.
     */
    public NameBasedWhereBuilder or(String property, NameBasedWhereBuilder other) {
	other = matching(null, property, other);
	return merge(getLeadingReferent(), property, closure(other), OR);
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder is typified with {@code referent} and {@code property}.
     */
    public NameBasedWhereBuilder or(Class<?> referent, String property, NameBasedWhereBuilder other) {
	other = matching(referent, property, other);
	return merge(referent, property, closure(other), OR);
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined in the this
     * builder.
     */
    public NameBasedWhereBuilder and(String property, NameBasedWhereBuilder other) {
	other = matching(null, property, other);
	return merge(getLeadingReferent(), property, closure(other), AND);
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder is typified with {@code referent} and {@code property}.
     */
    public NameBasedWhereBuilder and(Class<?> referent, String property, NameBasedWhereBuilder other) {
	other = matching(referent, property, other);
	return merge(referent, property, closure(other), AND);
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder();
	sb.append(" ( ");

	final Iterator<Expression> it = getExpressions().iterator();
	final Expression first = it.next();
	sb.append(first.toString());
	for (; it.hasNext();) {
	    sb.append(it.next());
	}

	sb.append(" ) ");
	return sb.toString().replaceAll("\\s+", " ");
    }
}
