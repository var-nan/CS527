package edu.iastate.cs527;

import edu.iastate.cs527.impl.LockBST;
import edu.iastate.cs527.impl.LockFreeBST;
import edu.iastate.cs527.impl.SerialBST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    // TODO study more about ThreadPool Executor service framework.
    // it is giving exception for boundedwork queue.
    public static void main(String[] args) {

        System.out.println("Number of available processors: " + Runtime.getRuntime().availableProcessors());
        final int runtimeProcessors = Runtime.getRuntime().availableProcessors();
        //Executor executor = Executors.newFixedThreadPool(100);

        /*
        Ok, here's the plan, I'm gonna measure execution with different threads 1,2,4,8,16.
        different size of tree.
         */

        //test(new LockBST<>(500));

        final int[] threads = new int[6];
        // initialize threads

        {
            var p = 0;
            for (int i = 1; i <= runtimeProcessors; i = i << 1) {
                threads[p++] = i;
            }
        }

        final int[] n_elements = {1000, 10000, 100000, 1000000};


        int init_elements = 50000;
        final int n = 6000;
        final int min = 1;
        final int max = 45000;

        //test(new SerialBST<>(25000), n, init_elements, min, max);

        // TODO Turn off GC.

        //

        BST<Integer> lfBst = new LockFreeBST<>(Integer.MAX_VALUE,
                Integer.MAX_VALUE-1, Integer.MAX_VALUE-2);
        BST<Integer> bst = new LockFreeBST<>(Integer.MAX_VALUE,
                Integer.MAX_VALUE-1, Integer.MAX_VALUE-2);
        lfBst.insert(25000);
        bst.insert(25000);

        // populate tree
        for (int i = 1; i <= init_elements ; i++){
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            lfBst.insert(x);
        }

        test(new SerialBST<>(25000),n, init_elements, min, max);

        List<Runnable> tasks = getTasks(n, lfBst, min, max);
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(tasks);
        int poolsize = 8;
        BSTThreadPool bstThreadPool = new BSTThreadPool(poolsize,poolsize,
                                                    100,
                                                    TimeUnit.MILLISECONDS,
                                                    workQueue);
        var start  = System.nanoTime();
        bstThreadPool.prestartAllCoreThreads();
        //var b = System.nanoTime();
        //long start = System.nanoTime();
        bstThreadPool.shutdown();
        while (!bstThreadPool.isShutdown());
        long end = System.nanoTime();
        //System.out.println("prestarting took: "+(b-a)/Math.pow(10,9) +" seconds");
        System.out.println("ThreadPool is finished: "+(end-start)/(Math.pow(10, 9)) +" seconds");
        System.out.println(lfBst.traverse().size());

        //ExecutorService bstThreadPool = Executors.newFixedThreadPool(100);
        /*
        long total = 0L;
        for (int i = 1; i <= n ; i++){
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            var start = System.nanoTime();
            bstThreadPool.execute(() -> lfBst.insert(x));
            var end = System.nanoTime();
            total += (end-start);

            //bstThreadPool.execute(() -> lfBst.search(x));
            //bstThreadPool.execute(() -> lfBst.delete(x));
        }

        //long end = System.nanoTime();
        // TODO catch RejectedExecutionException when the queue is full.
        //bstThreadPool.execute(() -> System.out.println("soem computattion doing"));
        bstThreadPool.shutdownNow();
        //System.out.println("Lock Free with "+n+" operations took: "+ ((total)/Math.pow(10,9)) +" Seconds");
        System.out.println(bstThreadPool.isTerminated());
        //printTree(lfBst.traverse());

         */
    }

    //public static void testLFBST(int n_elements)


    public static void test(BST<Integer> bst, int N, int init_elements, int min, int max) {

        //int min = 1;
        //int max = 45000;
        //int init_elements = 50000;
        //int N = 6000;

        // initialize the tree with init_number of elements
        for (int i = 0; i < init_elements; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max+1);
            bst.insert(x);
        }

        long total = 0L;

        for (int i = 0; i < N; i++) {
            var x = ThreadLocalRandom.current().nextInt(min, max);
            var start = System.nanoTime();
            bst.insert(x);
            var end = System.nanoTime();
            total += (end-start);
        }


        System.out.println("Serial version with "+ N +" operations took "+ ((total)/Math.pow(10,9))+ " seconds.");
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

    private static List<Runnable> getTasks(int n, BST<Integer> bst, int min, int max) {
        List<Runnable> tasks = new ArrayList<>(n);

        for (int i = 0; i < n; i++){
            var x = ThreadLocalRandom.current().nextInt(min, max);
            tasks.add(() -> bst.insert(x));
        }
        return tasks;
    }

    private static List<Runnable> get_compute_tasks(BST<? extends Number> bst, int n_tasks, Load load, int min, int max) {
        List<Runnable> tasks = new ArrayList<>();

        if (load == Load.WRITE_HEAVY) {

        }

        return null;


    }

    //private static void task_load()
}

enum Load {
    WRITE_HEAVY,
    READ_HEAVY,
    MIXED
}