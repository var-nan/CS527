package edu.iastate.cs527;

import edu.iastate.cs527.impl.LockBST;
import edu.iastate.cs527.impl.LockFreeBST;
import edu.iastate.cs527.impl.SerialBST;

import java.util.List;
import java.util.concurrent.*;

public class Main {

    // TODO study more about ThreadPool Executor service framework.
    // it is giving exception for boundedwork queue.
    public static void main(String[] args) {
        long start = System.nanoTime();
        long end = System.nanoTime();
        System.out.println("Number of available processors: "+ Runtime.getRuntime().availableProcessors());
        //Executor executor = Executors.newFixedThreadPool(100);
        //test(new SerialBST<>(500));
        //test(new LockBST<>(500));
        var n = 6;

        // TODO Turn off GC.
        BSTThreadPool bstThreadPool = new BSTThreadPool(8,8,
                                                    100000,
                                                    TimeUnit.MILLISECONDS,
                                                    new LinkedBlockingDeque<>(10));
        var min = 1;
        var max = 450;

        BST<Integer> lfBst = new LockFreeBST<>(500, 499, 498);
        System.out.println("Starting computation.");
        for (int i = 1; i <=n ; i++){
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            bstThreadPool.execute(() -> lfBst.insert(x));
            bstThreadPool.execute(() -> lfBst.search(x));
            bstThreadPool.execute(() -> lfBst.delete(x));
        }
        //bstThreadPool.execute(() -> System.out.println("soem computattion doing"));
        bstThreadPool.shutdown();
        System.out.println("Time elapsed: "+bstThreadPool.totalTime.get());
        printTree(lfBst.traverse());
    }


    public static void test(BST<Number> bst) {

        int min = 1;
        int max = 1000;
        int N = 30;

        for (int i = 0; i < N; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            bst.insert(x);
        }

        List<Number> elements = bst.traverse();
        for (Number n: elements){
            System.out.print(n+" ");
        }
        System.out.println("\nDone");
    }

    private static void printTree(List<Integer> list){
        System.out.println("printing tree..");
        for (Integer i: list){
            System.out.print(i +" ");
        }
    }

    private static void testLFBST(){

    }

    private static void testSerialBST(int n) {


    }
}