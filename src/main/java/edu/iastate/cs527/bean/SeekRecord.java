package edu.iastate.cs527.bean;

public class SeekRecord<T extends Number> {

    // TODO- CHANGED type AtomicReference to normal
    public NodeLF<T> ancestor, successor, parent, leaf;

    public NodeLF<T> getAncestor() {
        return ancestor;
    }

    public void setAncestor(NodeLF<T> ancestor) {
        this.ancestor = ancestor;
    }

    public NodeLF<T> getSuccessor() {
        return successor;
    }

    public void setSuccessor(NodeLF<T> successor) {
        this.successor = successor;
    }

    public NodeLF<T> getParent() {
        return parent;
    }

    public void setParent(NodeLF<T> parent) {
        this.parent = parent;
    }

    public NodeLF<T> getLeaf() {
        return leaf;
    }

    public void setLeaf(NodeLF<T> leaf) {
        this.leaf = leaf;
    }
}
