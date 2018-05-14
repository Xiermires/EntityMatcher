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
