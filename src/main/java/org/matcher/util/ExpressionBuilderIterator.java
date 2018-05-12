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
package org.matcher.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import org.matcher.builder.ExpressionBuilder;
import org.matcher.expression.Expression;

public class ExpressionBuilderIterator<E extends ExpressionBuilder<E>> implements Iterator<Expression<?>> {

    private final Deque<Iterator<Node<E>>> stack = new ArrayDeque<>();

    private Iterator<Expression<?>> currentExpressions = null;
    private Iterator<Node<E>> currentChildren = null;
    private ExpressionBuilder<E> nextNode = null;
    private Expression<?> next = null;

    public ExpressionBuilderIterator(ExpressionBuilder<E> node) {
	this.nextNode = node;
	if (nextNode.hasChildren()) {
	    currentChildren = nextNode.getChildren().iterator();
	} else {
	    currentChildren = Collections.emptyIterator();
	}

	currentExpressions = nextNode.getData().getExpressions().iterator();
	advance();
    }

    private void advance() {
	if (!currentExpressions.hasNext()) {
	    do {
		advanceNode();
		if (nextNode != null) {
		    currentExpressions = nextNode.getData().getExpressions().iterator();
		} else {
		    break;
		}
	    } while (!currentExpressions.hasNext());
	}

	if (!currentExpressions.hasNext()) {
	    next = null;
	} else {
	    next = currentExpressions.next();
	}
    }

    @Override
    public boolean hasNext() {
	return next != null;
    }

    @Override
    public Expression<?> next() {
	final Expression<?> current = next;
	advance();
	return current;
    }

    void advanceNode() {
	if (currentChildren.hasNext()) {
	    nextNode = currentChildren.next().getData();
	    if (nextNode.hasChildren()) {
		stack.push(currentChildren);
		currentChildren = nextNode.getChildren().iterator();
	    }
	} else {
	    reverseNode();
	}
    }

    void reverseNode() {
	if (!stack.isEmpty()) {
	    currentChildren = stack.pop();
	    advanceNode();
	} else {
	    nextNode = null;
	}
    }
}
