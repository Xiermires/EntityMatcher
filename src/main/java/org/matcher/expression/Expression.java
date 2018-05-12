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
import static org.matcher.builder.BuilderUtils.getTableName;
import static org.matcher.builder.BuilderUtils.toAlias;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;
import org.matcher.util.Arborescence;
import org.matcher.util.Node;

public abstract class Expression<T extends Operator, V> extends Arborescence<Expression<T, V>> {

    private final V value;
    private T operator;

    private Class<?> referent;
    private String property;

    public Expression(T operator) {
	this(operator, null);
    }

    public Expression(T operator, V value) {
	this.operator = operator;
	this.value = value;
    }

    /**
     * Resolves to a from expression or to an empty string if it can't be resolved.
     */
    public String resolveFromClause(Set<Class<?>> seenReferents) {
	return resolveReferent(getReferent(), seenReferents);
    }

    protected String resolveReferent(Class<?> referent, Set<Class<?>> seenReferents) {
	if (!seenReferents.contains(referent)) {
	    // update references
	    seenReferents.add(referent);

	    final String tableName = getTableName(referent);
	    return tableName + " " + toAlias(tableName);
	}
	return "";
    }

    /**
     * Resolves to an expression or to an empty string if it can't be resolved.
     */
    public String resolve(ParameterBinding bindings) {
	if (hasChildren()) {
	    final List<String> results = new ArrayList<>();
	    for (Node<Expression<T, V>> child : getChildren()) {
		results.add(child.getData().resolve(bindings));
	    }
	    return combine(results);
	} else {
	    return apply(aliasPlusColumn(getReferent(), getProperty()));
	}
    }

    protected String combine(List<String> results) {
	final StringBuilder sb = new StringBuilder();
	boolean appendSeparator = false;
	final Iterator<String> it = results.iterator();
	String next = it.next();
	if (next != null && !next.isEmpty()) {
	    sb.append(next);
	    appendSeparator = true;
	}
	while (it.hasNext()) {
	    if (appendSeparator) {
		sb.append(getResolveSeparator());
		appendSeparator = false;
	    }
	    next = it.next();
	    if (next != null && !next.isEmpty()) {
		sb.append(next);
		appendSeparator = true;
	    }
	}
	return apply(sb.toString());
    }

    protected String apply(String result) {
	return result;
    }

    protected String getResolveSeparator() {
	return "";
    }

    public T getOperator() {
	return operator;
    }

    public void setReferent(Class<?> referent) {
	this.referent = referent;
    }

    public Class<?> getReferent() {
	return referent;
    }

    public void setProperty(String property) {
	this.property = property;
    }

    public String getProperty() {
	return property;
    }

    public V getValue() {
	return value;
    }

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	if (getReferent() == null) {
	    setReferent(referent);
	}
	if (getProperty() == null) {
	    setProperty(property);
	}
	getChildren().forEach(node -> node.getData().overwriteNullReferenceAndProperties(referent, property));
    }
}