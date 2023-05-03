package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import edu.iastate.cs527.BSTThreadPool;
import edu.iastate.cs527.ProfileEvents;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A mointor based implementation of Binary Search Tree.
 * Performs all operations using synchronized blocks.
 * <p>
 * Class is ThreadSafe.
 * </p>
 *
 * @param <T>
 *
 * @author nandhan
 */
@ThreadSafe
public class LockBST<T extends Number> implements BST<T> {

    ProfileEvents.LockSearchTime searchTime = new ProfileEvents.LockSearchTime();

    Node<T> root;

    public LockBST(T key) {
        this.root = new Node<>(key);
    }

    /**
     * Performs search operation for the given key in the tree.
     * Called by insert() and delete().
     * @param key - element
     * @return -boolean true if key is present in the tree.
     */
    @Override
    public synchronized boolean search(T key) {
        // allow concurrent search by threads? problem?

        var current = root;

        while (current != null) {
            if (current.key.intValue() == key.intValue()) {
                return true;
            }
            if (compare(key,current.key )){
                current = current.left;
            }
            else current = current.right;
        }
        return false;
    }

    /**
     * Inserts the given key to the tree. If the key is already present
     * in the tree, method returns false, else perform key insertion and
     * returns true. The tree do not allow duplicates.
     *
     * @param key
     * @return - boolean
     */
    @Override
    public synchronized boolean insert(T key) {

        searchTime.begin();
        boolean find = search(key);
        searchTime.commit();

        if (find)
            return false;

        var current = root;

        while (current != null) {
            if (compare(key, current.key)){ // key < current.key
                if (current.left == null) {
                    current.left = new Node<>(key);
                    break;
                }
                else {
                    current = current.left;
                }
            }
            else {
                if (current.right == null){
                    current.right = new Node<>(key);
                    break;
                }
                else current = current.right;
            }
        }
        return false;
    }

    /**
     * returns boolean true if the  integer value of two keys of type T are equal.
     *
     * @param key1
     * @param key2
     * @return boolean value based on comparision.
     */
    private boolean equals(T key1, T key2) {
        return key1.intValue() == key2.intValue();
    }

    /**
     * Deletes the given key from the tree. Returns false, if key is
     * not present in tree.
     *
     * @param key - element to delete from tree.
     * @return - true if element is deleted, else false.
     */
    @Override
    public synchronized boolean delete(T key) {

        Node<T> parent = null;
        var current = root;

        while (current != null && !equals(current.key, key)){
            parent = current;

            if (compare(key, current.key))
                current = current.left;
            else current = current.right;
        }

        if (current == null)
            return false;

        if (current.left == null && current.right == null){
            // leaf node, just remove it.
            if (current != root){
                if (parent.left == current)
                    parent.left = null;
                else parent.right = null;
            }
            else this.root = null;
        }
        else if (current.left != null && current.right != null){
            Node<T> successor = getMinimum(current.right);
            T min_key = successor.key;

            // delete successor node
            delete(min_key);
            current.key = min_key;
        }
        else {
            // node has only one child.
            Node<T> child = current.left != null ? current.left: current.right;

            if (current != root){
                if (current == parent.left)
                    parent.left = child;
                else parent.right = child;
            }
            else root = child;
        }

        return true;
    }

    /**
     * Returns minimum node for a given node to replace the current
     * node. Helper method for delete().
     *
     * @param node
     * @return - node to replace the given node.
     */
    private Node<T> getMinimum(Node<T> node) {
        // pass right chiild.
        while( node.left != null)
            node = node.left;
        return node;
    }

    /**
     * Perform Traversal of the Tree.
     * @return List of elements of type T in sorted (ascending) order.
     */
    public synchronized List<T> traverse() {
        List<T> elements = new ArrayList<>();
        Node<T> current = this.root;
        inOrderTraversal(current, elements);
        return elements;
    }

    /**
     * Helper method for traverse().
     *
     * @param node - current node
     * @param elements - List to populate with elements
     */
    private void inOrderTraversal(Node<T> node, List<T> elements) {
        if (node!= null) {
            if (node.left != null)
                inOrderTraversal(node.left, elements);
            elements.add(node.key);
            if (node.right != null)
                inOrderTraversal(node.right, elements);
        }
    }

    /**
     * Compares two given keys based on their integer values.
     * If int value of key1 < int value of key2, returns true, else false.
     *
     * @param key1
     * @param key2
     * @return - (boolean) key1 < key2.
     */
    private boolean compare(T key1, T key2) {
        return key1.intValue() < key2.intValue();
    }

    public static void main(String[] args) {
        LockBST<Integer> lockBST = new LockBST<>(500);

        var numbers = ThreadLocalRandom.current().ints(100, 1,1000);

        BSTThreadPool threadPool = new BSTThreadPool(3,3,1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        numbers.forEach((i) -> threadPool.execute(()->lockBST.insert(i)) );

        threadPool.shutdown();

        while(!threadPool.isTerminated());

        System.out.println("Printing array");
        lockBST.traverse().forEach((i) -> System.out.print(i+" "));

    }


}
