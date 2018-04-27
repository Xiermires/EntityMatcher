package org.entitymatcher;

import java.util.Deque;

public interface Node<T>
{
    T getData();

    void setData(T data);

    Node<T> getParent();

    void setParent(Node<T> parent);

    boolean hasParent();

    void addChild(T child);

    Deque<Arborescence<T>> getChildren();

    boolean hasChildren();
}
