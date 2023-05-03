package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import edu.iastate.cs527.ProfileEvents;
import edu.iastate.cs527.bean.NodeLF;
import edu.iastate.cs527.bean.EdgeLF;
import edu.iastate.cs527.bean.SeekRecord;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock Free implementation of Concurrent Binary Search Tree.
 * For more details about the algorithm: <a href="https://dl.acm.org/doi/10.1145/2555243.2555256">Original Paper</a>
 *
 * Class is ThreadSafe.
 * @param <T>
 *
 * @author nandhan
 */
@ThreadSafe
public class LockFreeBST<T extends Number> implements BST<T> {

    static ProfileEvents.SeekRecordTime seekTime = new ProfileEvents.SeekRecordTime();

    public enum Mode {
        INJECTION, // INJECTION MODE
        CLEANUP // CLEANUP MODE.
    }

    // sentinel nodes.
    NodeLF<T> R, S;
    // S behaves like root node.

    public LockFreeBST(T inf2, T inf1, T inf0) {

        /* Initialization of Sentinel Nodes */
        R = new NodeLF<>(inf2);
        S = new NodeLF<>(inf1);
        EdgeLF<T> rightEdgeR = new EdgeLF<>(new NodeLF<>(inf2));
        R.right = new AtomicReference<>(rightEdgeR);
        EdgeLF<T> leftEdgeR = new EdgeLF<>(new NodeLF<>(inf1));
        R.left = new AtomicReference<>(leftEdgeR);

        var rightEdgeL = new EdgeLF<T>(new NodeLF<>(inf1));
        var leftEdgeL = new EdgeLF<T>(new NodeLF<>(inf0));
        S.right = new AtomicReference<>(rightEdgeL);
        S.left = new AtomicReference<>(leftEdgeL);
    }

    @Override
    public boolean search(T x) {
        // get seek record, and check if node is null or not.
        // getLeaf might return null.
        var seekRecord = seek(x);
        var leaf = seekRecord.getLeaf();
        if (leaf != null) {
            var key = leaf.getKey();
            return equals(key, x);
        }
        return false;
    }

    @Override
    public List<T> traverse(){
        // called from a single thread (main).
        List<T> list = new ArrayList<>();
        NodeLF<T> current = S.left.get().nodeAddr;
        if (current != null)
            inOrderTraverse(current, list);

        return list;
    }

    private void inOrderTraverse(NodeLF<T> node, List<T> elements) {
        //
        if (node != null) {
            if (node.left.get() != null)
                inOrderTraverse(node.left.get().getNodeAddr(), elements);
            //elements.add(node.key);
            if (node.right.get()!= null)
                inOrderTraverse(node.right.get().getNodeAddr(), elements);
            if (node.left.get() == null && node.right.get() == null)
                elements.add(node.key);
        }
    }

    @Override
    public boolean insert(T key) {

        while(true) {
            var seekRecord = seek(key); // obtain seek record.
            // if key is already present, return false.
            if (equals(seekRecord.getLeaf().getKey(), key))
                return false;
            else {
                // key is not present in tree.
                // extract parent and child nodes
                NodeLF<T> parent = seekRecord.getParent();
                NodeLF<T> leaf = seekRecord.getLeaf();

                AtomicReference<EdgeLF<T>> childEdge;
                if (lessThan( key, parent.getKey()))
                    childEdge = parent.left; // only to track which side key need to be inserted.
                else childEdge =  parent.right;

                // create new internal node and a new leaf node,
                NodeLF<T> newLeaf;
                NodeLF<T> newInternal;

                // get the corresponding edge for CAS.
                if (lessThan(key, leaf.getKey())){
                    // key is less than leaf key,
                    // new node should be (key, left = newNode, right = oldchild)
                    newLeaf = new NodeLF<>(key);
                    var sibling = new NodeLF<>(leaf.getKey());
                    newInternal = new NodeLF<>(leaf.getKey(), new EdgeLF<>(newLeaf), new EdgeLF<>(sibling)); // TODO CHAGNE THIS, create sibling edge
                }
                else {
                    // new node should be (key, left= oldchild, right = newNode)
                    newLeaf = new NodeLF<>(key);
                    var sibling = new NodeLF<>(leaf.getKey());
                    newInternal = new NodeLF<>(key, new EdgeLF<>(sibling),  new EdgeLF<>(newLeaf));
                }

                // write to tree using CAS.
                var result = childEdge.compareAndSet(childEdge.get(), new EdgeLF<>(newInternal)); // assuming it will work fine, else replace with parent.left or right

                if (result) {
                    return true;
                }
                else {
                    // insertion failed. help conflicting delete operation.
                    boolean flag = childEdge.get().isFlagged();
                    boolean tag = childEdge.get().isTagged();
                    NodeLF<T> childAddress = childEdge.get().getNodeAddr();
                    // if address not changed, then either leaf or sibling has been flagged for deletion.
                    if ((childAddress == leaf) && (flag || tag) )
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
            seekTime.begin();
            var seekRecord = seek(key);
            seekTime.commit();

            var parent = seekRecord.getParent();
            EdgeLF<T> child;
            //var less = lessThan(key, parent.getKey());
            if (lessThan(key, parent.getKey())) // which side the key lies to parent.
                child = parent.getLeft();
            else child = parent.getRight();

            //var leaf = seekRecord.getLeaf().get();
            if (mode == Mode.INJECTION) {
                // check if key is present in the tree.
                leaf = seekRecord.getLeaf();
                if (!equals(leaf.getKey(), key))
                    return false;
                // inject delete operation by marking the flag bit in edge from parent.
                boolean result = child.setFlaggedCAS(true); // BTS Operation
                if (result) {
                    mode = Mode.CLEANUP;
                    if (cleanUp(key, seekRecord))
                        return true;
                }
                else {
                    boolean flag = child.isFlagged();
                    boolean tag = child.isTagged();
                    NodeLF<T> childNode = child.getNodeAddr();

                    if ((childNode == leaf) && (flag || tag)) {
                        // address of child has not changed, either the leaf node or its sibling has been flagged.
                        cleanUp(key, seekRecord);
                    }
                }
            }
            else{
                // clean up mode.
                // check if the leaf node is still present or not.
                if (leaf != seekRecord.getLeaf()){
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

        //initialize seek record using sentinel nodes.
        SeekRecord<T> seekRecord = new SeekRecord<>();
        seekRecord.setAncestor(R);
        seekRecord.setSuccessor(S);
        seekRecord.setParent(S);
        seekRecord.setLeaf(S.getLeft().getNodeAddr());

        // extract parent. ParentField = seekrecord.parent.left(), current feild = seekrecord.leaf.left(), current = currentfiled.address
        // parentFields are Edges, not Nodes.
        EdgeLF<T> parentField = seekRecord.getParent().getLeft(); // both point to same address?
        EdgeLF<T> currentField = seekRecord.getLeaf().getLeft();

        if (currentField == null)
            return seekRecord;

        NodeLF<T> current = currentField.getNodeAddr();
        // current stores reference of Node.
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

            if (current.getLeft() == null || current.getRight() == null)
                return seekRecord;
            if (lessThan(key, current.getKey()))
                currentField = current.getLeft();
            else
                currentField = current.getRight();

            current = currentField.getNodeAddr();
        }
        // traversal complete.
        return seekRecord;
    }

    private boolean cleanUp(T key, SeekRecord<T> seekRecord) {

        NodeLF<T> ancestor = seekRecord.getAncestor();
        NodeLF<T> successor = seekRecord.getSuccessor();
        NodeLF<T> parent = seekRecord.getParent();
        NodeLF<T> leaf = seekRecord.getLeaf();

        EdgeLF<T> successorAddr, childAddr, siblingAddr;
        AtomicReference<EdgeLF<T>> successorEdge;

        // track which side of ancestor, successor lies. store pointer.
        if (lessThan(key, ancestor.getKey())) {
            successorEdge = ancestor.left;
            successorAddr = ancestor.getLeft();
        }
        else {
            successorEdge = ancestor.right;
            successorAddr = ancestor.getRight();
        }

        boolean lessThanParent = lessThan(key, parent.getKey());
        if (lessThanParent) {
            childAddr = parent.getLeft();
            siblingAddr = parent.getRight();
        }
        else {
            childAddr = parent.getRight();
            siblingAddr = parent.getLeft();
        }

        boolean flag = childAddr.isFlagged();
        if (!flag) {
            // leaf node is not flagged for deletion, thus sibling node must be flagged,
            siblingAddr = childAddr; // not clear why?
        }

        // tag sibling edge, no modify operation should occur from now on at this edge.
        siblingAddr.setTaggedCAS(true);

        // read flag and address fields.
        // perform CAS.
        return successorEdge.compareAndSet(successorAddr,siblingAddr );
    }

    private boolean lessThan(T key1, T key2) {
        // returns true, if key1 is less than key2.
        return key1.intValue() < key2.intValue();
    }

    private boolean equals(T actualKey, T newKey) {
        return actualKey.intValue() == newKey.intValue();
    }

    public Long[] inOrderTraversal() {
        return null;
    }

    public static void main(String[] args) {
        LockFreeBST<Integer> bst = new LockFreeBST<>(5000,4999, 4998);

        bst.insert(100);
        bst.insert(50);
        bst.insert(200);
        bst.insert(250);
        bst.insert(10);
        bst.delete(10);

        List<Integer> elements = bst.traverse();
        System.out.println("List of elements in Tree..");
        for (Integer i: elements)
            System.out.print(i +" ");
    }


}
