package org.matcher;

import java.util.Set;

public abstract class FromWhereBuilder<T extends ExpressionBuilder<T>> extends ExpressionBuilder<T> {

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder fromClause = new StringBuilder();
	final StringBuilder whereClause = new StringBuilder();

	parseExpressions(whereClause, fromClause, seenReferents, bindings);

	final StringBuilder sb = new StringBuilder().append("FROM ").append(fromClause);
	if (whereClause.length() > 0) {
	    sb.append(" ").append("WHERE ").append(whereClause);
	}
	return sb.toString();
    }

    @Override
    protected String getResolveFromSeparator() {
	return ", ";
    }

    @Override
    protected String getResolveSeparator() {
	return " ";
    }
}
