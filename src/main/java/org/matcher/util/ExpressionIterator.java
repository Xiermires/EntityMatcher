package org.matcher.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import org.matcher.builder.ExpressionBuilder;
import org.matcher.expression.Expression;

public class ExpressionIterator<E extends ExpressionBuilder<E>> implements Iterator<Expression<?, ?>> {

    private final Deque<Iterator<Node<E>>> stack = new ArrayDeque<>();

    private Iterator<Expression<?, ?>> currentExpressions = null;
    private Iterator<Node<E>> currentChildren = null;
    private ExpressionBuilder<E> nextNode = null;
    private Expression<?, ?> next = null;

    public ExpressionIterator(ExpressionBuilder<E> node) {
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
    public Expression<?, ?> next() {
	final Expression<?, ?> current = next;
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
