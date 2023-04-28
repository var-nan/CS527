package edu.iastate.cs527;

import edu.iastate.cs527.impl.LockFreeBST;
import edu.iastate.cs527.impl.SerialBST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Timing {

    /*
    TODO:
    * 1. Increase iteration count.
    * 2. Modify hotspot flags for GC.
    * 3. Profile Heap.
    * 4. Turn off GC.
    * 5. Measure time for serial BST with single thread.
    * 6. Create Lock version of BST.
    * 7. Modify getTasks() to initialize tasks for multiple tasks.
     */

    final static int n_iterations = 5;
    final static int[] initial_tree_size = {10000, 20000, 50000}; /// todo revert to 10K, 20K, 100K
    final static int n_operations = 15000; // todo revert to 15K

    final static int[] readHeavy = loadModeArray(Load.READ_HEAVY);
    final static int[] writeHeavy = loadModeArray(Load.WRITE_HEAVY);
    final static int[] mixed = loadModeArray(Load.MIXED);

    final static int serialPoolSize = 1;
    final static int serialMaxPoolSize = 1;
    final static long keepAliveTime = 1000; // todo change this.
    final static TimeUnit timeUnit = TimeUnit.MILLISECONDS; // TODO CHANGE THIS

    //final static int[] nThreads;

    public static void main(String[] args) {


        var processors = Runtime.getRuntime().availableProcessors();
        var nelements = (int) (Math.log(processors)/Math.log(2));

        int[] nThreads = new int[nelements];
        nThreads[0] = 1;
        for (int i = 1; i < nelements; i++) {
            nThreads[i] = nThreads[i-1] * 2;
        }

        for (Integer threads: nThreads) {

            for (Integer tree_size : initial_tree_size) {

                for (Load load : Load.values()) {
                    for (int iter = 0; iter < n_iterations; iter++) {

                        BST<Integer> serialBST = new SerialBST<>(tree_size / 2);
                        BST<Integer> lfBST = new LockFreeBST<>(tree_size + 3, tree_size + 2, tree_size + 1);

                        populateTree(tree_size, serialBST, lfBST);

                        IntStream randomNumbers = ThreadLocalRandom.current().ints(n_operations, 1, tree_size); // TODO; add range and size.


                        if (threads == 1) {
                            IntStream randomNumbers2 = ThreadLocalRandom.current().ints(n_operations, 1, tree_size);
                            List<Runnable> serialTasks = getTasks(serialBST, randomNumbers2, load);
                            LinkedBlockingQueue<Runnable> serialWorkQueue = new LinkedBlockingQueue<>(serialTasks);
                            BSTThreadPool serialPool = new BSTThreadPool(1, 1, keepAliveTime, timeUnit, serialWorkQueue);
                            long startTimeSerial = System.nanoTime();
                            serialPool.prestartAllCoreThreads();
                            serialPool.shutdown();
                            while (!serialPool.isTerminated()) ;
                            long endTimeSerial = System.nanoTime();

                            System.out.println("For serial BST, iteration: " + iter + " execution time: " + (endTimeSerial - startTimeSerial) / Math.pow(10, 9));
                        }
                        /*
                        // create workqueue for lock free.
                        List<Runnable> serialTasks = getTasks(serialBST, randomNumbers, mode);
                        LinkedBlockingQueue<Runnable> serialWorkQueue = new LinkedBlockingQueue<>(serialTasks);
                        // execute queue.

                        BSTThreadPool serialPool = new BSTThreadPool(1, 1, keepAliveTime, timeUnit, serialWorkQueue);
                        // start

                        long startTime = System.nanoTime();
                        int currentThreads = serialPool.prestartAllCoreThreads();

                        serialPool.shutdown();
                        while(!serialPool.isShutdown());

                        long endTime = System.nanoTime();

                        System.out.println("In "+iter+ " iteration, exeuction with ")

                         */
                        // lock free version
                        List<Runnable> lfTasks = getTasks(lfBST, randomNumbers, load);
                        LinkedBlockingQueue<Runnable> lfWorkQueue = new LinkedBlockingQueue<>(lfTasks);

                        //System.out.println("Starting tasks.... in iteration: " +iter+ "\n");
                        BSTThreadPool lfPool = new BSTThreadPool(threads, threads, keepAliveTime, timeUnit, lfWorkQueue);

                        long startTimeLF = System.nanoTime();
                        lfPool.prestartAllCoreThreads();

                        lfPool.shutdown();
                        /*
                        try{
                            lfPool.awaitTermination(1000, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Not shutdown");
                        }

                         */

                        while(!lfPool.isTerminated()){

                        }

                        long endTimeLF = System.nanoTime();

                        String printStatement = "Threads: " + threads + ". Initial Tree Size: " + tree_size +
                                ". Mode: " + load + ". Iteration: " + iter +
                                " Execution time: "  + (endTimeLF - startTimeLF)/(Math.pow(10,9)) + " seconds";

                        if (lfPool.isTerminated())
                            System.out.println(printStatement);
                        else System.out.println("Threadpool is not shtudown yet.");

                    }
                }
            }
        }

    }

    @SafeVarargs
    private static void populateTree(int tree_size, BST<Integer>... bst) {

        // ALL TREES MUST HAVE SAME NODES.

        IntStream numbers= ThreadLocalRandom.current().ints(tree_size, 1, tree_size);

        // insert roots for all trees
        int root = tree_size/2;

        // insert root
         for (BST<Integer> tree: bst) {
             tree.insert(root);
         }

         // insert number in each of trees.
         numbers.forEach((i) -> {
             for (BST<Integer> tree: bst)
                 tree.insert(i);
         });

        /*
        bst.insert(tree_size/2); // root node

        for (int i = 1; i <= tree_size;i ++) {
            int x = ThreadLocalRandom.current().nextInt(1, tree_size);
            bst.insert(x);
        }

         */
    }

    private static int[] getRandomArray(int start, int end, int size) {
        int [] array = new int[size];

        for (int i = start; i < end; i++) {
            array[i] = ThreadLocalRandom.current().nextInt(start, end);
        }

        var numbers = ThreadLocalRandom.current().ints(size, start, end);
        numbers.forEach((i) -> array[0] = i);

        return array;
    }

    private static int[] loadModeArray(Load load) {

        int[] type = new int[100];
        int insertStart, insertEnd, searchStart, searchEnd, deleteStart, deleteEnd;


        // TODO VERIFY INDICES.
        if (load == Load.WRITE_HEAVY) {
            insertStart = 0;
            insertEnd = 50;
            searchStart = 50;
            searchEnd = 50;
            deleteStart = 50;
            deleteEnd = 100;

        }
        else if (load == Load.READ_HEAVY) {
            insertStart = 0;
            insertEnd = 9;
            searchStart = 9;
            searchEnd = 99;
            deleteStart = 99;
            deleteEnd = 100;
        }
        else {
            insertStart = 0;
            insertEnd = 20;
            searchStart = 20;
            searchEnd = 90;
            deleteStart = 90;
            deleteEnd = 100;
        }

        for ( int i = insertStart; i< insertEnd; i++) {
            type[i] = 1;
        }
        for (int i = searchStart; i < searchEnd; i++) {
            type[i] = 2;
        }
        for (int i = deleteStart; i < deleteEnd; i++){
            type[i] = 3;
        }

        return type;
    }
    private static int[] loadArray(int insertL, int searchL, int deleteL, int tree_size, Load load) {

        if (searchL + deleteL + insertL != 100) {
            throw new RuntimeException("Invalid Load balance");
        }

        //var elements = new int[n_operations];

        //var elements = getRandomArray(1, tree_size, n_operations);

        //var int_range = (int) insertL * n_operations;
        //var search_range = (int) searchL * n_operations;
        //var delete_range = (int) deleteL * n_operations;

        /*
        for (int i = 0; i < n_operations; i++) {
            int x = ThreadLocalRandom.current().nextInt(1, tree_size);
            elements[i] = x;
        }

         */

        /*
        write heavy = 50% insert, 0% search, 50% delete
        read heavy = 9% insert, 80% search, 1% delete
        mixed = 20% insert, 60% search, 20% delete
         */

        int[] type = new int[100];
        int insertStart, insertEnd, searchStart, searchEnd, deleteStart, deleteEnd;


        // TODO VERIFY INDICES.
        if (load == Load.WRITE_HEAVY) {
            insertStart = 0;
            insertEnd = 50;
            searchStart = 50;
            searchEnd = 50;
            deleteStart = 50;
            deleteEnd = 100;

        }
        else if (load == Load.READ_HEAVY) {
            insertStart = 0;
            insertEnd = 9;
            searchStart = 9;
            searchEnd = 99;
            deleteStart = 99;
            deleteEnd = 100;
        }
        else {
            insertStart = 0;
            insertEnd = 20;
            searchStart = 20;
            searchEnd = 90;
            deleteStart = 90;
            deleteEnd = 100;
        }

        for ( int i = insertStart; i< insertEnd; i++) {
            type[i] = 1;
        }
        for (int i = searchStart; i < searchEnd; i++) {
            type[i] = 2;
        }
        for (int i = deleteStart; i < deleteEnd; i++){
            type[i] = 3;
        }

        return type;
    }

    private static List<Runnable> getTasks( BST<Integer> bst, IntStream randomNumbers, Load load) {

        List<Runnable> tasks = new ArrayList<>(n_operations);
        // get type array
        int [] type;
        if (load == Load.MIXED)
            type = mixed;
        else if (load == Load.WRITE_HEAVY)
            type = writeHeavy;
        else type = readHeavy;

        //IntStream randomNumbers = ThreadLocalRandom.current().ints(n_operations,1, tree_size );
        //System.out.println("In loading function: thread " + Thread.currentThread().getName());
        IntStream numbers = ThreadLocalRandom.current().ints(n_operations, 1,100);
        /*
        1 - insert
        2 - search
        3 - delete
         */
        //tasks.add(() -> bst.insert(100));

        /*
        var randomIterator = randomNumbers.iterator();
        var numbersArray = numbers.toArray();

        for (Integer i: numbersArray) {
            if (randomIterator.hasNext()) {
                if (type[i] == 1) {
                    tasks.add(() -> bst.insert(randomIterator.nextInt()));
                }
                else if (type[i] == 2)
                    tasks.add(() -> bst.search(randomIterator.nextInt()));
            }
        }

         */


        var randomIterator = randomNumbers.iterator();
        numbers.forEach( (i) -> {//

            if (randomIterator.hasNext()) {

                int nextNumber = randomIterator.nextInt();
                if (type[i] == 1)
                    tasks.add(() -> bst.insert(nextNumber));
                else if (type[i] == 2)
                    tasks.add(() -> bst.search(nextNumber));
                else if (type[i] == 3)
                    tasks.add(() -> bst.delete(nextNumber));
                //else tasks.add(() -> bst.search(100));
            }

        });

        return tasks;
    }
}

enum Load {
    WRITE_HEAVY,
    READ_HEAVY,
    MIXED;
}
