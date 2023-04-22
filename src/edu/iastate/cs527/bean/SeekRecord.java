package edu.iastate.cs527.bean;

import java.util.concurrent.atomic.AtomicReference;

public class SeekRecord<T extends Number> {

    public AtomicReference<NodeLF<T>> ancestor, successor, parent, leaf;

    public AtomicReference<NodeLF<T>> getAncestor() {
        return ancestor;
    }

    public void setAncestor(AtomicReference<NodeLF<T>> ancestor) {
        this.ancestor = ancestor;
    }

    public AtomicReference<NodeLF<T>> getSuccessor() {
        return successor;
    }

    public void setSuccessor(AtomicReference<NodeLF<T>> successor) {
        this.successor = successor;
    }

    public AtomicReference<NodeLF<T>> getParent() {
        return parent;
    }

    public void setParent(AtomicReference<NodeLF<T>> parent) {
        this.parent = parent;
    }

    public AtomicReference<NodeLF<T>> getLeaf() {
        return leaf;
    }

    public void setLeaf(AtomicReference<NodeLF<T>> leaf) {
        this.leaf = leaf;
    }
}
