package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import edu.iastate.cs527.BSTThreadPool;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

// @ThreadSafe
public class LockBST<T extends Number> implements BST<T> {

    Node<T> root;

    public LockBST(T key) {
        this.root = new Node<>(key);
    }

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

    @Override
    public synchronized boolean insert(T key) {
        if (search(key))
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

    private boolean equals(T key1, T key2) {
        return key1.intValue() == key2.intValue();
    }

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

    private Node<T> getMinimum(Node<T> node) {
        // pass right chiild.
        while( node.left != null)
            node = node.left;
        return node;
    }

    public synchronized List<T> traverse() {
        List<T> elements = new ArrayList<>();
        Node<T> current = this.root;
        inOrderTraversal(current, elements);
        return elements;
    }

    private void inOrderTraversal(Node<T> node, List<T> elements) {
        if (node!= null) {
            if (node.left != null)
                inOrderTraversal(node.left, elements);
            elements.add(node.key);
            if (node.right != null)
                inOrderTraversal(node.right, elements);
        }
    }

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
