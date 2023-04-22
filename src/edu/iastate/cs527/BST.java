package edu.iastate.cs527;

import java.util.List;

public interface BST<T extends Number> {

    public boolean search(T key);



    public boolean insert(T key);

    public boolean delete(T key);

    public List<T> traverse();

}
