package edu.iastate.cs527.bean;

/**
 * Data Type that holds node references.
 * <p>Class has 4 fields:
 *  1. ancestor
 *  2. successor
 *  3. parent
 *  4. leaf
 *  </p>
 * @param <T>
 *
 * @author nandhan
 */
public class SeekRecord<T extends Number> {

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
