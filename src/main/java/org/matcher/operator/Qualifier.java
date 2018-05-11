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
package org.matcher.operator;

import org.matcher.parameter.ParameterBinding;


public class Qualifier<T> extends Operator implements Negatable {
    
    private final String affirmed;
    private final String negated;
    
    private boolean isNegated;

    public Qualifier(String affirmed, String negated) {
        super(affirmed);
        this.affirmed = affirmed;
        this.negated = negated;
        this.isNegated = false;
    }
    
    public String resolve(String tableColumn, ParameterBinding bindings, T param) {
        return tableColumn + getSymbol() + bindings.createParam(param);
    }

    @Override
    public void negate() {
        setSymbol(isNegated ? affirmed : negated);
        isNegated = !isNegated;
    }
    
    public boolean isNegated() {
	return isNegated;
    }
}