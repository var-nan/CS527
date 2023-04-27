package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class SerialBST<T extends Number> implements BST<T> {

    // TODO complete delete function.

    // create root node
    Node<T> root;

    public SerialBST(T root) {
        this.root = new Node<>(root);

    }

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

    @Override
    public boolean insert(T key) {

        // don't allow duplicates
        if (search(key))
            return false;

        Node<T> current = root;

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

    private Node<T> getMinimum(Node<T> node) {
        // pass right chiild.
        while( node.left != null)
            node = node.left;
        return node;
    }
    @Override
    public boolean delete(T key) {

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

    private boolean compare(T key1, T key2){
        return key1.intValue() < key2.intValue();
        //return false; // TODO ADD CODE.
    }

    private boolean equals(T key1, T key2) {
        return key1.intValue() == key2.intValue();
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

    public static void main(String[] args) {
        SerialBST<Integer> bst = new SerialBST<>(500);

        int min = 1;
        int max = 1000;
        int N = 30;

        int[] vals = new int[N];

        for (int i = 0; i < N; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            vals[i] = x;
            bst.insert(x);
        }

        for (int i = 0; i < 11; i++){
            bst.delete(vals[i]);
        }


        List<Integer> elements = bst.traverse();
        System.out.println("Number of elemets present: " + elements.size());
        for (Integer n: elements){
            System.out.print(n+" ");
        }
    }
}

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