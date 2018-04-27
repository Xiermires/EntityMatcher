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
package org.entitymatcher;

import java.util.ArrayDeque;
import java.util.Deque;

public class Arborescence<T> implements Node<T>
{
    private T data;
    private Node<T> parent;
    private Deque<Arborescence<T>> children;

    public Arborescence()
    {
        this(null, null);
    }

    public Arborescence(T data)
    {
        this(data, null);
    }

    public Arborescence(T data, Arborescence<T> parent)
    {
        this.data = data;
        this.parent = parent;
        children = new ArrayDeque<>();
    }

    @Override
    public T getData()
    {
        return data;
    }

    @Override
    public void setData(T data)
    {
        this.data = data;
    }

    @Override
    public Node<T> getParent()
    {
        return parent;
    }

    @Override
    public void setParent(Node<T> parent)
    {
        this.parent = parent;
    }

    @Override
    public boolean hasParent()
    {
        return parent != null;
    }

    @Override
    public void addChild(T child)
    {
        children.add(new Arborescence<T>(child, this));
    }

    @Override
    public Deque<Arborescence<T>> getChildren()
    {
        return children;
    }

    @Override
    public boolean hasChildren()
    {
        return !children.isEmpty();
    }
}
