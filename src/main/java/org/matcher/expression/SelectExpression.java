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

import static org.matcher.BuilderUtils.getColumnName;
import static org.matcher.BuilderUtils.getTableName;
import static org.matcher.BuilderUtils.tableColumn;
import static org.matcher.BuilderUtils.toAlias;
import static org.matcher.Expressions.NONE;

import java.util.Iterator;

import org.matcher.Node;
import org.matcher.ParameterBinding;
import org.matcher.operator.Selector;

import com.google.common.base.Strings;

public class SelectExpression<T> extends NonResolvingExpression<Selector, Object> {

    public SelectExpression(Class<T> referent) {
	super(NONE);
	setReferent(referent);
    }

    public SelectExpression(Class<T> referent, SelectExpression<?> expression) {
	super(NONE);
	setReferent(referent);
	addChild(expression);
    }

    public SelectExpression(SelectExpression<T> expression) {
	this(NONE, expression);
    }

    public SelectExpression(Selector operator, SelectExpression<?> expression) {
	super(operator);
	setReferent(expression.getReferent());
	setProperty(expression.getProperty());
	addChild(expression);
    }

    public SelectExpression(Selector operator, String property) {
	super(operator);
	setProperty(property);
    }

    public SelectExpression(Selector operator, Class<T> referent, String property) {
	super(operator);
	setProperty(property);
	setReferent(referent);
    }

    public SelectExpression(Selector operator) {
	super(operator);
    }

    public SelectExpression(String property) {
	super(NONE);
	setProperty(property);
    }

    @Override
    @SuppressWarnings("unchecked")
    // safe
    public Class<T> getReferent() {
	return (Class<T>) super.getReferent();
    }

    @Override
    public String resolve(ParameterBinding unused) {
	final StringBuilder sb = new StringBuilder();
	if (hasChildren()) {
	    final Iterator<Node<Expression<Selector, Object>>> it = getChildren().iterator();
	    final Expression<Selector, Object> first = it.next().getData();
	    sb.append(resolveExpression(first));

	    while (it.hasNext()) {
		sb.append(getResolveSeparator());
		sb.append(resolveExpression(it.next().getData()));
	    }
	    return getOperator().resolve(sb.toString());
	} else {
	    return resolveExpression(this);
	}
    }

    protected String getResolveSeparator() {
	return ", ";
    }

    private String resolveExpression(Expression<Selector, Object> expression) {
	final StringBuilder sb = new StringBuilder();
	if (expression.hasChildren()) {
	    for (Node<Expression<Selector, Object>> child : expression.getChildren()) {
		final Expression<Selector, Object> data = child.getData();
		sb.append(resolveExpression(data));
	    }
	    return expression.getOperator().resolve(sb.toString());
	} else {
	    final String table = getTableName(expression.getReferent());
	    final String alias = toAlias(table);
	    final String column = getColumnName(expression.getReferent(), expression.getProperty());
	    return expression.getOperator().resolve(tableColumn(alias, column));
	}
    }

    @Override
    public String toString() {
	final String ref = getReferent() == null ? "?" : getReferent().getSimpleName();
	final String prop = getProperty() == null ? "?" : getProperty();
	return Strings.isNullOrEmpty(getOperator().toString()) ? ref + "." + prop : getOperator() + "(" + ref + "."
		+ prop + ")";
    }
}