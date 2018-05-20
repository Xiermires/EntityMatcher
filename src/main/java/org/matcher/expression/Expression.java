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

import java.util.ArrayList;
import java.util.List;

import org.matcher.parameter.ParameterBinding;
import org.matcher.util.Arborescence;
import org.matcher.util.Node;

public class Expression extends Arborescence<Expression> {

    private String operator = "";

    private Class<?> referent;
    private String property;

    public Expression() {
	setData(this);
    }

    /**
     * Resolves to an expression or to an empty string if it can't be resolved.
     */
    public String resolve(ParameterBinding bindings) {
	if (hasChildren()) {
	    final List<String> results = new ArrayList<>();
	    for (Node<Expression> child : getChildren()) {
		results.add(child.getData().resolve(bindings));
	    }
	    return combine(results);
	} else {
	    return apply(aliasPlusColumn(getReferent(), getProperty()));
	}
    }

    protected String combine(List<String> results) {
	final StringBuilder sb = new StringBuilder();
	for (String result : results) {
	    if (result != null) {
		sb.append(result);
	    }
	}
	return apply(sb.toString());
    }

    protected String apply(String result) {
	return result;
    }

    public String getOperator() {
	return operator;
    }

    protected void setOperator(String operator) {
	this.operator = operator;
    }

    public void setReferent(Class<?> referent) {
	this.referent = referent;
    }

    public Class<?> getReferent() {
	return referent;
    }

    public void setProperty(String property) {
	this.property = property;
    }

    public String getProperty() {
	return property;
    }

    public void overwriteNullReferenceAndProperties(Class<?> referent, String property) {
	if (getReferent() == null) {
	    setReferent(referent);
	}
	if (getProperty() == null) {
	    setProperty(property);
	}
	getChildren().forEach(node -> node.getData().overwriteNullReferenceAndProperties(referent, property));
    }
}