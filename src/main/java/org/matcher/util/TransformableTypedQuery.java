package org.matcher.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

public class TransformableTypedQuery<T, F> implements TypedQuery<T> {

    private final TypedQuery<F> delegate;
    private final Function<? super F, ? extends T> transformer;

    public TransformableTypedQuery(TypedQuery<F> query, Function<F, T> tranformer) {
	this.delegate = query;
	this.transformer = tranformer;
    }

    @Override
    public int executeUpdate() {
	return delegate.executeUpdate();
    }

    @Override
    public int getMaxResults() {
	return delegate.getMaxResults();
    }

    @Override
    public int getFirstResult() {
	return delegate.getFirstResult();
    }

    @Override
    public Map<String, Object> getHints() {
	return delegate.getHints();
    }

    @Override
    public Set<Parameter<?>> getParameters() {
	return delegate.getParameters();
    }

    @Override
    public Parameter<?> getParameter(String name) {
	return delegate.getParameter(name);
    }

    @Override
    public <X> Parameter<X> getParameter(String name, Class<X> type) {
	return delegate.getParameter(name, type);
    }

    @Override
    public Parameter<?> getParameter(int position) {
	return delegate.getParameter(position);
    }

    @Override
    public <X> Parameter<X> getParameter(int position, Class<X> type) {
	return delegate.getParameter(position, type);
    }

    @Override
    public boolean isBound(Parameter<?> param) {
	return delegate.isBound(param);
    }

    @Override
    public <X> X getParameterValue(Parameter<X> param) {
	return delegate.getParameterValue(param);
    }

    @Override
    public Object getParameterValue(String name) {
	return delegate.getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
	return delegate.getParameterValue(position);
    }

    @Override
    public FlushModeType getFlushMode() {
	return delegate.getFlushMode();
    }

    @Override
    public LockModeType getLockMode() {
	return delegate.getLockMode();
    }

    @Override
    public <X> X unwrap(Class<X> cls) {
	return delegate.unwrap(cls);
    }

    @Override
    public List<T> getResultList() {
	return delegate.getResultList().stream().map(transformer).collect(Collectors.toList());
    }

    @Override
    public T getSingleResult() {
	return transformer.apply(delegate.getSingleResult());
    }

    @Override
    public TypedQuery<T> setMaxResults(int maxResult) {
	delegate.setMaxResults(maxResult);
	return this;
    }

    @Override
    public TypedQuery<T> setFirstResult(int startPosition) {
	delegate.setFirstResult(startPosition);
	return this;
    }

    @Override
    public TypedQuery<T> setHint(String hintName, Object value) {
	delegate.setHint(hintName, value);
	return this;
    }

    @Override
    public <X> TypedQuery<T> setParameter(Parameter<X> param, X value) {
	delegate.setParameter(param, value);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
	delegate.setParameter(param, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
	delegate.setParameter(param, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Object value) {
	delegate.setParameter(name, value);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Calendar value, TemporalType temporalType) {
	delegate.setParameter(name, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Date value, TemporalType temporalType) {
	delegate.setParameter(name, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Object value) {
	delegate.setParameter(position, value);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Calendar value, TemporalType temporalType) {
	delegate.setParameter(position, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Date value, TemporalType temporalType) {
	delegate.setParameter(position, value, temporalType);
	return this;
    }

    @Override
    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
	delegate.setFlushMode(flushMode);
	return this;
    }

    @Override
    public TypedQuery<T> setLockMode(LockModeType lockMode) {
	delegate.setLockMode(lockMode);
	return this;
    }
}
