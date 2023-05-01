package edu.iastate.cs527.bean;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Edge for Lock Free Binary Search Tree.
 * <p>
 * class has three attributes:
 *  1. nodeAddr (Node)
 *  2. flag (AtomicBoolean)
 *  3. tag (AtomicBoolean)
 *
 * flag - to track if both parent and child nodes are marked for deletion.
 * tag - to track if the child node is marked for deletion.
 * </p>
 * <p>
 * All the updates to atomic attributes are performed with Atomic
 * Compare-And-Swap instructions.
 * </p>
 *
 * The class is ThreadSafe.
 *
 * @param <T> - T extends Number
 *
 * @author nandhan
 */
@ThreadSafe
public class EdgeLF<T extends Number> {

    /*
    flag - flag bit
    tag - tag bit
    nodeAddr - address of the node (basically the node itself).
     */
    public AtomicBoolean flagged;
    public AtomicBoolean tagged;
    public NodeLF<T> nodeAddr; // does nodeAddr have to be AtomicReference?

    public EdgeLF(NodeLF<T> nodeAddr) {
        this.flagged = new AtomicBoolean(false);
        this.tagged = new AtomicBoolean(false);
        this.nodeAddr = nodeAddr;
    }

    /*
    Below are getter and setter methods for attributes.
     */
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
