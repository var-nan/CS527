package edu.iastate.cs527;

import edu.iastate.cs527.impl.LockBST;
import edu.iastate.cs527.impl.SerialBST;

import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        long start = System.nanoTime();
        long end = System.nanoTime();
        System.out.println(Runtime.getRuntime().availableProcessors());
        //Executor executor = Executors.newFixedThreadPool(100);
        //test(new SerialBST<>(500));
        //test(new LockBST<>(500));

        BSTThreadPool bstThreadPool = new BSTThreadPool(4,4,
                100000,
                TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>(10));

        bstThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("soem computattion doing");
            }
        });

        bstThreadPool.shutdown();
        var time = bstThreadPool.totalTime;
        //System.out.println("Time elapsed: "+ (end-start));
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
}