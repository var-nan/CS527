package edu.iastate.cs527.bean;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Edge<T extends Number> {

    // TODO use readwrite locks?
    // TODO create constructor

    /*
    flag - flag bit
    tag - tag bit
    nodeAddr - address of the node (basically the node itself).
     */
    public AtomicBoolean flagged; // edit: changing atomic boolean to boolaen
    public AtomicBoolean tagged;
    public NodeLF<T> nodeAddr; // does nodeAddr have to be AtomicReference?

    public Edge(NodeLF<T> nodeAddr) {
        this.flagged = new AtomicBoolean(false);
        this.tagged = new AtomicBoolean(false);
        this.nodeAddr = nodeAddr;
    }

    public boolean isFlagged() {
        return flagged.get();
    }

    public boolean setFlaggedCAS(boolean flagged) {
        // set value with CAS
        return this.flagged.compareAndSet(false, true);
    }

    public boolean isTagged() {
        return tagged.get();
    }

    public boolean setTaggedCAS(boolean tagged) {
        return this.tagged.compareAndSet(false, true);
    }

    public NodeLF<T> getNodeAddr() {
        return nodeAddr;
    }

    public void setNodeAddr(NodeLF<T> nodeAddr) {
        this.nodeAddr = nodeAddr;
    }
}
