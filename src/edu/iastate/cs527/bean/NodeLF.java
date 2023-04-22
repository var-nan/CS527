package edu.iastate.cs527.bean;

import java.util.concurrent.atomic.AtomicReference;

public class NodeLF<T extends Number>{
    public T key;
    public AtomicReference<NodeBit<T>> left;
    public AtomicReference<NodeBit<T>> right;

    // constructor

    public NodeLF() {
    }

    public NodeLF(T key) {
        this.key = key; // TODO connect left and right childs
    }


    public NodeLF(T key, NodeBit<T> left, NodeBit<T> right) {
        this.key = key;
        this.left.set(left);
        this.right.set(right);
    }

    // TODO make updates as atomic updates.


    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public NodeBit<T> getLeft() {
        return left.get();
    }

    public boolean setLeftCAS(NodeBit<T> left, NodeBit<T> newLeft) {
        return this.left.compareAndSet( left, newLeft);
    }

    public NodeBit<T> getRight() {
        return right.get();
    }

    public void setRightCAS(NodeBit<T> right, NodeBit<T> newRight) {
        this.right.compareAndSet(right, newRight);
    }
}
