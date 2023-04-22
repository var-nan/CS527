package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import edu.iastate.cs527.bean.Mode;
import edu.iastate.cs527.bean.NodeLF;
import edu.iastate.cs527.bean.NodeBit;
import edu.iastate.cs527.bean.SeekRecord;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeBST<T extends Number> implements BST<T> {

    // TODO create 4 sentinel nodes.

    // TODO CREATE ROOT NODE.
    NodeLF<T> root;

    NodeLF<T> R, S;


    public LockFreeBST(T r, T s) {
        //R.left = S;
        R = new NodeLF<>(r);
        S = new NodeLF<>(s);
        R.left = S;

        Number n = Integer.MAX_VALUE;


        root = new NodeLF<>(r);
        //root.setLeftCAS();

    }

    @Override
    public boolean search(T x) {
        // get seek record, and check if node is null or not.
        // getLeaf might return null.
        var seekRecord = seek(x);
        var leaf = seekRecord.getLeaf().get();
        if (leaf != null) {
            var key = leaf.getKey();
            return equals(key, x);
        }
        return false;
    }

    @Override
    public List<T> traverse(){return null;}

    @Override
    public boolean insert(T key) {

        while(true) {

            var seekRecord = seek(key); // obtain seek record.

            // if key is already present, return false.
            if (equals(seekRecord.getLeaf().get().getKey(), key))
                return false;
            else {
                // key is not present in tree.
                // extract parent and child nodes.
                var parent = seekRecord.getParent();
                var leaf = seekRecord.getLeaf();

                NodeBit<T> child; // todo - atomic reference.
                if (lessThan(parent.get().getKey(), key))
                    child = parent.get().getLeft();
                else child =  parent.get().getRight();

                // create new internal node and a new leaf node,
                var newLeaf = new NodeLF<T>(key, null, null);
                var newInternal = new NodeLF<T>();

                var newNodebit = new NodeBit<>(new AtomicReference<>(newLeaf));

                // if parent key is less than
                if (lessThan(key, leaf.get().getKey())){
                    // key is less than leaf key,
                    // new node should be (key, left = newNode, right = oldchild)
                    newInternal = new NodeLF<T>(leaf.get().getKey(),newNodebit , child);
                }
                else {
                    // new node should be (key, left= oldchild, right = newNode)
                    newInternal = new NodeLF<>(key, child, newNodebit);
                }

                // todo write to tree using CAS.
                boolean result = child.getNodeAddr().compareAndSet(leaf.get(), newInternal);

                if (result) {
                    return true;
                }
                else {
                    // insertion failed. help conflicting delete operation.
                    boolean flag = child.isFlagged();
                    boolean tag = child.isTagged();
                    var childAddress = child.getNodeAddr();

                    // if address not changed, then either leaf or sibling has been flagged for deletion.
                    if ((childAddress.get() == leaf.get()) && (flag || tag) )
                        cleanUp(key, seekRecord);
                    // else restart process.
                }
            }
        }
    }

    @Override
    public boolean delete(T key) {

        var mode = Mode.INJECTION;
        NodeLF<T> leaf = null;
        while (true) {
            // obtain seek record.
            var seekRecord = seek(key);

            var parent = seekRecord.getParent().get();

            var less = lessThan(key, parent.getKey());
            NodeBit<T> child;
            if (less)
                child = parent.getLeft();
            else child = parent.getRight();

            //var leaf = seekRecord.getLeaf().get();
            if (mode == Mode.INJECTION) {
                leaf = seekRecord.getLeaf().get();
                if (!equals(leaf.getKey(), key))
                    return false;
                // key is present in the tree.
                // inject delete operation by marking the flag bit.
                boolean result = child.setFlaggedCAS(true); // TODO cas operation

                if (result) {
                    mode = Mode.CLEANUP;
                    if (cleanUp(key, seekRecord))
                        return true;
                }
                else {
                    boolean flag = child.isFlagged();
                    boolean tag = child.isTagged();
                    var childNode = child.getNodeAddr().get();

                    if ((childNode == leaf) && (flag || tag)) {
                        // address of child has not changed, either the leaf node or its sibling has been flagged.
                        cleanUp(key, seekRecord);
                    }
                }
            }
            else{
                // non-injection mode. clean up mode.
                // check if the leaf node is still present or not.
                if (leaf != seekRecord.getLeaf().get()){
                   return true;
                }
                else {
                    // leaf is still present in tree; remove it
                    boolean done = cleanUp(key, seekRecord);
                    if (done)
                        return true;
                }
            }
        }
    }

    private SeekRecord<T> seek(T key) {
        // seek doesn't need any CAS instructions.
        /*
        seek traverses from root of the tree to the key called access path.
        It sets 4 pointers: to ancestor node, successor node, parent node, leaf node.
         */

        // TODO initialize seek record using sentinel nodes.

        SeekRecord<T> seekRecord = new SeekRecord<>();

        // TODO change this.
        seekRecord.setAncestor(null);
        seekRecord.setSuccessor(null);
        seekRecord.setParent(null);
        seekRecord.setLeaf(null);

        // extract parent. ParentField = seekrecord.parent.left(), current feild = seekrecord.leaf.left(), current = currentfiled.address
        var parentField = seekRecord.getParent().get().getLeft(); // both point to same address?
        var currentField = seekRecord.getLeaf().get().getLeft();
        var current = currentField.getNodeAddr();
        // current stores atomic reference of Node.
        /*
        Traverse from root to leaf
         */
        while (current != null) {
            /*
            If no conflicting delete is in progress, then successor is same as parent.
             */
            if (!parentField.isTagged()) {
                // advance ancestor and successor pointers
                seekRecord.setAncestor(seekRecord.getParent());
                seekRecord.setSuccessor(seekRecord.getLeaf());
            }
            // advance parent and leaf pointers
            seekRecord.setParent(seekRecord.getLeaf());
            seekRecord.setLeaf(current);

            parentField = currentField;
            // update other variables used in traversal
            // todo create new comparator
            if (lessThan(key, current.get().getKey()))
                currentField = current.get().getLeft();
            else
                currentField = current.get().getRight();

            current = currentField.getNodeAddr();
        }
        // traversal complete.
        return seekRecord;
    }

    private boolean cleanUp(T key, SeekRecord<T> seekRecord) {

        var ancestor = seekRecord.getAncestor();
        var successor = seekRecord.getSuccessor();
        var parent = seekRecord.getParent();
        var leaf = seekRecord.getLeaf();

        boolean less = lessThan(key, ancestor.get().getKey());

        NodeBit<T> successorAddr, childAddr, siblingAddr;
        if (less) {
            successorAddr = ancestor.get().getLeft();
        }
        else {
            successorAddr = ancestor.get().getRight();
        }

        boolean lessThanParent = lessThan(key, parent.get().getKey());
        if (lessThanParent) {
            childAddr = parent.get().getLeft();
            siblingAddr = parent.get().getRight();
        }
        else {
            childAddr = parent.get().getRight();
            siblingAddr = parent.get().getLeft();
        }

        boolean flag = childAddr.isFlagged();
        if (!flag) {
            // leaf node is not flagged for deletion, it must be flagged
            // TODO read logic again
        }

        // tag sibling edge, no modify operation should occur from now on at this edge.

        siblingAddr.setTaggedCAS(true);

        boolean siblingFlag = siblingAddr.isFlagged();
        // todo check logic.
        return successorAddr.getNodeAddr().compareAndSet(successor.get(), siblingAddr.getNodeAddr().get());
    }

    private boolean lessThan(T key1, T key2) {
        // returns true, if key1 is less than key2.
        return true;
    }

    private boolean equals(T actualKey, T newKey) {
        return true; // TODO
    }

    public Long[] inOrderTraversal() {
        return null;
    }
}
