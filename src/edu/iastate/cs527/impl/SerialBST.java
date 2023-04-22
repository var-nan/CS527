package edu.iastate.cs527.impl;

import edu.iastate.cs527.BST;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class SerialBST<T extends Number> implements BST<T> {

    // create root node
    Node<T> root;
    int count;

    public SerialBST(T root) {
        this.root = new Node<>(root);
        this.count++;
    }

    @Override
    public boolean search(T key) {
        Node<T> current = root;

        while (current != null) {
            T nodeKey = current.key;
            if (Objects.equals(nodeKey, key)) // todo check this.
                return true;
            else {
                if (compare(key, nodeKey)) { // todo create new comparator
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
        this.count++;
        return true;
    }

    @Override
    public boolean delete(T key) {

        if (!search(key))
            return false;
        var current = root;
        while (current != null){

            if (key.longValue() < current.key.longValue()){
                //
            }
        }
        this.count--;
        return true;
    }

    private boolean compare(T key1, T key2){
        return key1.intValue() < key2.intValue();
        //return false; // TODO ADD CODE.
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

        for (int i = 0; i < N; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            bst.insert(x);
        }

        List<Integer> elements = bst.traverse();
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

class NumberComparator<T extends Comparable<T>> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }
}
