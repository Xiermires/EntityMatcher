package org.matcher.bean;

import static org.matcher.bean.BeanBasedMatcher.getPropertyName;

import java.util.Deque;

import org.matcher.ExpressionBuilder;
import org.matcher.ParameterBinding;
import org.matcher.bean.InvokationCapturer.Capture;
import org.matcher.expression.BindingExpression;
import org.matcher.expression.Expression;
import org.matcher.expression.OperatorExpression;
import org.matcher.name.NameBasedExpressionBuilder;
import org.matcher.operator.Operator;

public class BeanBasedExpressionBuilder extends ExpressionBuilder<BeanBasedExpressionBuilder> {

    private final NameBasedExpressionBuilder delegate;

    public BeanBasedExpressionBuilder(NameBasedExpressionBuilder wrapped) {
	this.delegate = wrapped;
    }

    @Override
    protected BeanBasedExpressionBuilder getThis() {
	return this;
    }

    @Override
    public Deque<Expression<?, ?>> getExpressions() {
	return delegate.getExpressions();
    }

    public <E> BeanBasedExpressionBuilder or(E property, BeanBasedExpressionBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.or(getPropertyName(lastCapture), other.delegate);
	return this;
    }

    public <E> BeanBasedExpressionBuilder and(E property, BeanBasedExpressionBuilder other) {
	final Capture lastCapture = InvokationCapturer.getLastCapture();
	delegate.and(getPropertyName(lastCapture), other.delegate);
	return this;
    }

    @Override
    public String build(ParameterBinding bindings) {
	return delegate.build(bindings);
    }

    @Override
    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	delegate.overwriteNullReferenceAndProperties(referent, property);
    }

    @Override
    protected BeanBasedExpressionBuilder mergeAfterLastExpression(//
	    Class<?> referent, //
	    String property, //
	    BeanBasedExpressionBuilder other, //
	    Operator operator) {

	other.delegate.getExpressions().addFirst(new OperatorExpression(operator));
	other.delegate.getExpressions().addFirst(new BindingExpression(referent, property));
	delegate.addChild(other.delegate);
	return getThis();
    }
}
