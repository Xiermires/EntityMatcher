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

import java.util.Set;

import org.matcher.ParameterBinding;
import org.matcher.operator.Qualifier;

public class QualifierExpression<T> extends Expression<Qualifier<T>, T> {

    public QualifierExpression(Qualifier<T> qualifier, T value) {
	super(qualifier, value);
    }

    @Override
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
	return null;
    }

    @Override
    public String resolve(ParameterBinding bindings) {
	final String alias = toAlias(getTableName(getReferent()));
	final String column = getColumnName(getReferent(), getProperty());

	return getOperator().resolve(tableColumn(alias, column), bindings, getValue());
    }

    @Override
    public String toString() {
	final String ref = getReferent() == null ? "?" : getReferent().getSimpleName();
	final String prop = getProperty() == null ? "?" : getProperty();
	return ref + "." + prop + getOperator() + getValue();
    }
}
