package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
// @ThreadSafe
public class LockBST<T extends Number> implements BST<T> {

    Node<T> root;

    public LockBST(T key) {
        this.root = new Node<>(key);
    }

    @Override
    public boolean search(T key) {

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
    public boolean insert(T key) {
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

    @Override
    public boolean delete(T key) {
        return false;
    }

    public List<T> traverse() {
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
        return key1.intValue() < key1.intValue();
    }


}
