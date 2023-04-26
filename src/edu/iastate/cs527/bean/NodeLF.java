package edu.iastate.cs527.bean;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class NodeLF<T extends Number>{
    public final T key; // TODO make it final.
    public AtomicReference<Edge<T>> left = new AtomicReference<>();
    public AtomicReference<Edge<T>> right = new AtomicReference<>();

    // constructor

    public NodeLF(T key) {
        this.key = key; // TODO connect left and right childs
    }


    public NodeLF(T key, Edge<T> left, Edge<T> right) {
        this.key = key;
        this.left.set(left);
        //this.left = new AtomicReference<>(left);
        this.right.set(right);
    }

    // TODO make updates as atomic updates ?


    public T getKey() {
        return key;
    }

    public Edge<T> getLeft() {
        return left.get();
    }

    public boolean setLeftCAS(Edge<T> left, Edge<T> newLeft) {
        return this.left.compareAndSet( left, newLeft);
    }

    public Edge<T> getRight() {
        return right.get();
    }

    public void setRightCAS(Edge<T> right, Edge<T> newRight) {
        this.right.compareAndSet(right, newRight);
    }
}
