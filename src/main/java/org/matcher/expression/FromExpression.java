package org.matcher.expression;

import static org.matcher.BuilderUtils.getTableName;
import static org.matcher.BuilderUtils.toAlias;
import static org.matcher.Expressions.NONE;

import java.util.Set;

import org.matcher.operator.Operator;

public class FromExpression extends NonResolvingExpression<Operator, Object> {

    public FromExpression(Class<?> referent) {
	super(NONE);
	setReferent(referent);
    }

    @Override
    public String resolveFromClause(Set<Class<?>> seenReferents) {
	if (!seenReferents.contains(getReferent())) {
	    // update references
	    seenReferents.add(getReferent());

	    final String tableName = getTableName(getReferent());
	    return tableName + " " + toAlias(tableName);
	}
	return "";
    }
}
