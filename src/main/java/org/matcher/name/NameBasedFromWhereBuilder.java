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

import java.util.Iterator;

import org.matcher.builder.FromWhereBuilder;
import org.matcher.expression.Expression;
import org.matcher.parameter.ParameterBinding;
import org.matcher.util.Node;

/**
 * This class allows creating expressions chaining different operators as defined in the {@link NameBasedExpressions}
 * class.
 * <p>
 * {@link NameBasedFromWhereBuilder} are a tree structure which root node is typeless initially and typified later on
 * when {@link #build(Class, ParameterBinding)}.
 * <p>
 * While typifying an {@link NameBasedFromWhereBuilder}, all typeless children are identically typified.
 */
public class NameBasedFromWhereBuilder extends FromWhereBuilder<NameBasedFromWhereBuilder> {

    public NameBasedFromWhereBuilder(Expression<?> expression) {
	super(expression);
    }

    @Override
    public NameBasedFromWhereBuilder getThis() {
	return this;
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined in the this
     * builder.
     */
    public NameBasedFromWhereBuilder or(String property, NameBasedFromWhereBuilder other) {
	return mergeAfterLastExpression(null, property, other.hasChildren() ? closure(other) : other, OR);
    }

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder is typified with {@code referent} and {@code property}.
     */
    public NameBasedFromWhereBuilder or(Class<?> referent, String property, NameBasedFromWhereBuilder other) {
	return mergeAfterLastExpression(referent, property, other.hasChildren() ? closure(other) : other, OR);
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined in the this
     * builder.
     */
    public NameBasedFromWhereBuilder and(String property, NameBasedFromWhereBuilder other) {
	return mergeAfterLastExpression(null, property, other.hasChildren() ? closure(other) : other, AND);
    }

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder is typified with {@code referent} and {@code property}.
     */
    public NameBasedFromWhereBuilder and(Class<?> referent, String property, NameBasedFromWhereBuilder other) {
	return mergeAfterLastExpression(referent, property, other.hasChildren() ? closure(other) : other, AND);
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder();
	sb.append(" ( ");

	final Iterator<Expression<?>> it = getExpressions().iterator();
	final Expression<?> first = it.next();
	sb.append(first.toString());
	for (; it.hasNext();) {
	    sb.append(it.next());
	}

	for (Node<NameBasedFromWhereBuilder> node : getChildren()) {
	    final NameBasedFromWhereBuilder child = node.getData();
	    sb.append(child.toString());
	}
	sb.append(" ) ");
	return sb.toString().replaceAll("\\s+", " ");
    }
}
