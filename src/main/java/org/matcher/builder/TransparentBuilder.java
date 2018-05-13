package org.matcher.builder;

import java.util.Set;

import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public class TransparentBuilder extends ExpressionBuilder<TransparentBuilder> {

    public static final TransparentBuilder INSTANCE = new TransparentBuilder();

    protected TransparentBuilder() {
    }

    @Override
    protected TransparentBuilder getThis() {
	return this;
    }

    @Override
    protected Operator getOrOperator() {
	return null;
    }

    @Override
    protected Operator getAndOperator() {
	return null;
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	return "";
    }

    @Override
    protected String getResolveFromSeparator() {
	return "";
    }

    @Override
    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
    }
}
