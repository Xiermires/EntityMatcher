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
package org.matcher;

import static org.matcher.name.NameBasedExpressions.selection;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.matcher.expression.SelectExpression;
import org.matcher.name.NameBasedExpressionBuilder;

/**
 * An {@link EntityManager} wrapper which allows finding elements using the {@link NameBasedExpressionBuilder} to
 * compose jpql expressions.
 */
@SuppressWarnings("rawtypes")
public class EntityMatcher implements EntityManager {
    private final EntityManager delegate;

    public EntityMatcher(EntityManager delegate) {
	this.delegate = delegate;
    }

    /**
     * Returns a single element of type {@code clazz} matching the jpql expression.
     * <p>
     * Equivalent to call {@link Query#getSingleResult()} on the built query.
     */
    public <T, E extends ExpressionBuilder<E>> T findUnique(Class<T> clazz, E builder) {
	return createTypedQuery(clazz, selection(clazz), builder).getSingleResult();
    }

    /**
     * Returns a single element's selectExpression defined properties matching the jpql expression.
     * <p>
     * Equivalent to call {@link Query#getSingleResult()} on the built query.
     */
    public <T, E extends ExpressionBuilder<E>> T findUnique( //
	    Class<T> returnType, //
	    SelectExpression selectExpression, //
	    E whereBuilder) {
	return createTypedQuery(returnType, selectExpression, whereBuilder).getSingleResult();
    }

    /**
     * Returns a collection of elements of type {@code clazz} matching the jpql expression.
     * <p>
     * Equivalent to call {@link Query#getResultList()} on the built query.
     */
    public <T, E extends ExpressionBuilder<E>> List<T> find(Class<T> clazz, E builder) {
	return createTypedQuery(clazz, selection(clazz), builder).getResultList();
    }

    /**
     * Returns a single element's selectExpression defined properties matching the jpql expression.
     * <p>
     * Equivalent to call {@link Query#getResultList()} on the built query.
     */
    public <T, E extends ExpressionBuilder<E>> List<T> find( //
	    Class<T> returnType, //
	    SelectExpression selectExpression, //
	    E whereBuilder) {
	return createTypedQuery(returnType, selectExpression, whereBuilder).getResultList();
    }

    private <T, E extends ExpressionBuilder<E>> TypedQuery<T> createTypedQuery( //
	    Class<T> returnType, //
	    SelectExpression selector, //
	    E builder) {

	builder.overwriteNullReferenceAndProperties(selector.getReferent(), null);
	selector.overwriteNullReferenceAndProperties(selector.getReferent(), null);

	final ParameterBinding bindings = new ParameterBindingImpl();
	final StringBuilder queryBuilder = new StringBuilder();
	queryBuilder.append("SELECT ");
	queryBuilder.append(selector.resolve(bindings));
	queryBuilder.append(" ");
	queryBuilder.append(builder.build(bindings));
	final String queryTxt = queryBuilder.toString().replaceAll("\\s+", " ").trim();

	final TypedQuery<T> query = delegate.createQuery(queryTxt, returnType);
	bindings.resolveParams(queryTxt, query);
	return query;
    }

    // delegated methods

    @Override
    public void clear() {
	delegate.clear();
    }

    @Override
    public void close() {
	delegate.close();
    }

    @Override
    public boolean contains(Object arg0) {
	return delegate.contains(arg0);
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> arg0) {
	return delegate.createEntityGraph(arg0);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String arg0) {
	return delegate.createEntityGraph(arg0);
    }

    @Override
    public Query createNamedQuery(String arg0) {
	return delegate.createNamedQuery(arg0);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
	return delegate.createNamedQuery(arg0, arg1);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String arg0) {
	return delegate.createNamedStoredProcedureQuery(arg0);
    }

    @Override
    public Query createNativeQuery(String arg0) {
	return delegate.createNativeQuery(arg0);
    }

    @Override
    public Query createNativeQuery(String arg0, Class arg1) {
	return delegate.createNativeQuery(arg0, arg1);
    }

    @Override
    public Query createNativeQuery(String arg0, String arg1) {
	return delegate.createNativeQuery(arg0, arg1);
    }

    @Override
    public Query createQuery(String arg0) {
	return delegate.createQuery(arg0);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
	return delegate.createQuery(arg0);
    }

    @Override
    public Query createQuery(CriteriaUpdate arg0) {
	return delegate.createQuery(arg0);
    }

    @Override
    public Query createQuery(CriteriaDelete arg0) {
	return delegate.createQuery(arg0);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
	return delegate.createQuery(arg0, arg1);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String arg0) {
	return delegate.createStoredProcedureQuery(arg0);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String arg0, Class... arg1) {
	return delegate.createStoredProcedureQuery(arg0, arg1);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String arg0, String... arg1) {
	return delegate.createStoredProcedureQuery(arg0, arg1);
    }

    @Override
    public void detach(Object arg0) {
	delegate.detach(arg0);
    }

    @Override
    public <T> T find(Class<T> arg0, Object arg1) {
	return delegate.find(arg0, arg1);
    }

    @Override
    public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
	return delegate.find(arg0, arg1, arg2);
    }

    @Override
    public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
	return delegate.find(arg0, arg1, arg2);
    }

    @Override
    public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2, Map<String, Object> arg3) {
	return delegate.find(arg0, arg1, arg2, arg3);
    }

    @Override
    public void flush() {
	delegate.flush();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
	return delegate.getCriteriaBuilder();
    }

    @Override
    public Object getDelegate() {
	return delegate.getDelegate();
    }

    @Override
    public EntityGraph<?> getEntityGraph(String arg0) {
	return delegate.getEntityGraph(arg0);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> arg0) {
	return delegate.getEntityGraphs(arg0);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
	return delegate.getEntityManagerFactory();
    }

    @Override
    public FlushModeType getFlushMode() {
	return delegate.getFlushMode();
    }

    @Override
    public LockModeType getLockMode(Object arg0) {
	return delegate.getLockMode(arg0);
    }

    @Override
    public Metamodel getMetamodel() {
	return delegate.getMetamodel();
    }

    @Override
    public Map<String, Object> getProperties() {
	return delegate.getProperties();
    }

    @Override
    public <T> T getReference(Class<T> arg0, Object arg1) {
	return delegate.getReference(arg0, arg1);
    }

    @Override
    public EntityTransaction getTransaction() {
	return delegate.getTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
	return delegate.isJoinedToTransaction();
    }

    @Override
    public boolean isOpen() {
	return delegate.isOpen();
    }

    @Override
    public void joinTransaction() {
	delegate.joinTransaction();
    }

    @Override
    public void lock(Object arg0, LockModeType arg1) {
	delegate.lock(arg0, arg1);
    }

    @Override
    public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
	delegate.lock(arg0, arg1, arg2);
    }

    @Override
    public <T> T merge(T arg0) {
	return delegate.merge(arg0);
    }

    @Override
    public void persist(Object arg0) {
	delegate.persist(arg0);
    }

    @Override
    public void refresh(Object arg0) {
	delegate.refresh(arg0);
    }

    @Override
    public void refresh(Object arg0, Map<String, Object> arg1) {
	delegate.refresh(arg0, arg1);
    }

    @Override
    public void refresh(Object arg0, LockModeType arg1) {
	delegate.refresh(arg0, arg1);
    }

    @Override
    public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
	delegate.refresh(arg0, arg1, arg2);
    }

    @Override
    public void remove(Object arg0) {
	delegate.remove(arg0);
    }

    @Override
    public void setFlushMode(FlushModeType arg0) {
	delegate.setFlushMode(arg0);
    }

    @Override
    public void setProperty(String arg0, Object arg1) {
	delegate.setProperty(arg0, arg1);
    }

    @Override
    public <T> T unwrap(Class<T> arg0) {
	return delegate.unwrap(arg0);
    }
}
