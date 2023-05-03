package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import edu.iastate.cs527.Profiling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serial (Single Thread) implementation of Binary Search Tree.
 * This class is not threadsafe and should not be called from
 * concurrent threads.
 *
 * @author nandhan
 *
 * @param <T>
 */
public class SerialBST<T extends Number> implements BST<T> {

    Profiling.SearchTime searchTime = new Profiling.SearchTime();

    // create root node
    Node<T> root;

    public SerialBST(T root) {
        this.root = new Node<>(root);
    }

    /**
     * Performs search operation for the given key in the tree.
     * Called by insert() and delete().
     * @param key - element
     * @return -boolean true if key is present in the tree.
     */
    @Override
    public boolean search(T key) {
        Node<T> current = root;

        while (current != null) {
            T nodeKey = current.key;
            if (equals(nodeKey, key))
                return true;
            else {
                if (compare(key, nodeKey)) {
                    current = current.left;
                }
                else
                    current = current.right;
            }
        }
        return false;
    }

    /**
     * Inserts the given key to the tree. If the key is already present
     * in the tree, method returns false, else perform key insertion and
     * returns true. The tree do not allow duplicates.
     *
     * @param key
     * @return
     */
    @Override
    public boolean insert(T key) {

        // don't allow duplicates
        searchTime.begin();
        boolean find = search(key);
        searchTime.commit();

        if (find)
            return false;

        Node<T> current = root;

        // traverse through tree and find valid spot for the key.
        while(current != null) {
            if (compare(key, current.key)) {
                if (current.left == null) {
                    current.left = new Node<T>(key);
                    break;
                }
                current = current.left;
            }
            else {
                if (current.right == null){
                    current.right = new Node<>(key);
                    break;
                }
                current = current.right;
            }
        }
        return true;
    }

    private boolean isLeaf(Node<T> node) {
        return node.left == null && node.right == null;
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
     * Deletes the given key from the tree. Returns false, if key is
     * not present in tree.
     *
     * @param key - element to delete from tree.
     * @return - true if element is deleted, else false.
     */
    @Override
    public boolean delete(T key) {

        Node<T> parent = null;
        var current = root;

        // traverse through tree and find key's node.
        while (current != null && !equals(current.key, key)){
            parent = current;

            if (compare(key, current.key))
                current = current.left;
            else current = current.right;
        }

        // if key is not present in tree, return false.
        if (current == null)
            return false;

        /*
        TODO: complete documentation.
         */
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
     * Compares two given keys based on their integer values.
     * If int value of key1 < int value of key2, returns true, else false.
     *
     * @param key1
     * @param key2
     * @return - (boolean) key1 < key2.
     */
    private boolean compare(T key1, T key2){
        return key1.intValue() < key2.intValue();
        //return false; // TODO ADD CODE.
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
     * Perform Traversal of the Tree.
     * @return List of elements of type T in sorted (ascending) order.
     */
    public List<T> traverse() {
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

    public static void main(String[] args) {
        SerialBST<Integer> bst = new SerialBST<>(500);

        int min = 1;
        int max = 1000;
        int N = 30;

        int[] vals = new int[N];

        //IntStream numbers = ThreadLocalRandom.current().ints(10, min, max+1);

        for (int i = 0; i < N; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            vals[i] = x;
            bst.insert(x);
        }

        for (int i = 0; i < 11; i++){
            var result = bst.delete(vals[i]);
            if (result)
                System.out.println("Deleted Sucessfully: "+ vals[i]);
            else System.out.println("Not deleted "+ vals[i]);
        }


        List<Integer> elements = bst.traverse();
        System.out.println("Number of elemets present: " + elements.size());
        for (Integer n: elements){
            System.out.print(n+" ");
        }
    }
}

/**
 * Node type for Serial BST.
 * @param <T> - T extends Number
 */
class Node<T extends Number> {
    T key;
    Node<T> left;
    Node<T> right;

    public Node(T key) {
        this.key = key;
        this.left = null;
        this.right = null;
    }
}