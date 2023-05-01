package edu.iastate.cs527.bean;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Node for Lock Free Binary Search Tree.
 * <p>
 *     Class has three attributes:
 *     1. key - (T) that holds the data.
 *     2. left - AtomicReference to left edge (EdgeLF)
 *     3. right - AtomicReference to right edge (EdgeLF)
 * </p>
 *
 * this class is ThreadSafe.
 *
 * @param <T>
 *
 * @author nandhan
 */
@ThreadSafe
public class NodeLF<T extends Number>{
    public final T key; // TODO make it final.
    public AtomicReference<EdgeLF<T>> left = new AtomicReference<>();
    public AtomicReference<EdgeLF<T>> right = new AtomicReference<>();

    // constructor

    public NodeLF(T key) {
        this.key = key; // TODO connect left and right childs
    }


    public NodeLF(T key, EdgeLF<T> left, EdgeLF<T> right) {
        this.key = key;
        this.left.set(left);
        //this.left = new AtomicReference<>(left);
        this.right.set(right);
    }

    /* getters and setters for fields. */


    public T getKey() {
        return key;
    }

    public EdgeLF<T> getLeft() {
        return left.get();
    }

    public boolean setLeftCAS(EdgeLF<T> left, EdgeLF<T> newLeft) {
        return this.left.compareAndSet( left, newLeft);
    }

    public EdgeLF<T> getRight() {
        return right.get();
    }

    public void setRightCAS(EdgeLF<T> right, EdgeLF<T> newRight) {
        this.right.compareAndSet(right, newRight);
    }
}
