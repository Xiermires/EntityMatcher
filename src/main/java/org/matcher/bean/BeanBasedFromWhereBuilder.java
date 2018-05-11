package org.matcher.bean;

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;

import java.util.Deque;
import java.util.Set;

import org.matcher.builder.FromWhereBuilder;
import org.matcher.parameter.ParameterBinding;
import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.expression.BindingExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.OperatorExpression;
import org.matcher.name.NameBasedFromWhereBuilder;
import org.matcher.operator.Operator;

public class BeanBasedFromWhereBuilder extends FromWhereBuilder<BeanBasedFromWhereBuilder> {

    private final NameBasedFromWhereBuilder delegate;

    public BeanBasedFromWhereBuilder(NameBasedFromWhereBuilder wrapped) {
	this.delegate = wrapped;
    }

    @Override
    protected BeanBasedFromWhereBuilder getThis() {
	return this;
    }

    @Override
    public Deque<Expression<?, ?>> getExpressions() {
	return delegate.getExpressions();
    }

    public <E> BeanBasedFromWhereBuilder or(E property, BeanBasedFromWhereBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.or(getPropertyName(lastCapture), other.delegate);
	return this;
    }

    public <E> BeanBasedFromWhereBuilder and(E property, BeanBasedFromWhereBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.and(getPropertyName(lastCapture), other.delegate);
	return this;
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	return delegate.build(seenReferents, bindings);
    }

    @Override
    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	delegate.overwriteNullReferenceAndProperties(referent, property);
    }

    @Override
    protected BeanBasedFromWhereBuilder mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    BeanBasedFromWhereBuilder other, //
	    Operator operator) {

	other.delegate.getExpressions().addFirst(new OperatorExpression(operator));
	other.delegate.getExpressions().addFirst(new BindingExpression(referent, property));
	delegate.addChild(other.delegate);
	return getThis();
    }
}
