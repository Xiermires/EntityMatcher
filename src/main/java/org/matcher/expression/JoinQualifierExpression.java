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

import static org.matcher.builder.BuilderUtils.aliasPlusColumn;

import java.util.Set;

import javax.persistence.Query;

import org.matcher.operator.NegatableOperator;
import org.matcher.parameter.ParameterBinding;

/**
 * Where clause inner join.
 */
public class JoinQualifierExpression extends QualifierExpression<String> {

    private static final ParameterBinding joinBindings = new ParameterBinding() {
	@Override
	public String createParam(Object o) {
	    return (String) o;
	}

	@Override
	public void resolveParams(String rawQuery, Query query) {
	    throw new UnsupportedOperationException("Cannot bind.");
	}
    };
    
    private final Class<?> otherReferent;

    public JoinQualifierExpression(NegatableOperator qualifier, Class<?> otherReferent, String otherProperty) {
	super(qualifier, aliasPlusColumn(otherReferent, otherProperty));
	this.otherReferent = otherReferent;
    }

    @Override
    public String resolveFromClause(Set<Class<?>> seenReferents) {
	final StringBuilder sb = new StringBuilder();
	final String main = super.resolveFromClause(seenReferents);
	if (main != null && !main.isEmpty()) {
	    sb.append(main);
	    sb.append(", ");
	}
	sb.append(resolveReferent(otherReferent, seenReferents));
	return sb.toString();
    }

    @Override
    public String resolve(ParameterBinding bindings) {
	return super.resolve(joinBindings);
    }
}
