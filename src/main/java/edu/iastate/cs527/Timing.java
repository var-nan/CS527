package edu.iastate.cs527;

import edu.iastate.cs527.impl.LockBST;
import edu.iastate.cs527.impl.LockFreeBST;
import edu.iastate.cs527.impl.SerialBST;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Measures the performance of various implemntations of Binary Search Tree
 * under various scenarios. Writes the resuls to files (as serializable objects).
 *
 * @author nandhan
 */
public class Timing {

    /*
    TODO:
    * 1. Turn off GC.
    * 2. Modify hotspot flags for GC.
    * 3. Profile Heap.
     */

    final static int n_iterations = 5;
    final static int[] initial_tree_size = {1000, 2000, 5000}; /// todo revert to 10K, 20K, 100K
    final static int n_operations = 1500; // todo revert to 15K

    final static int[] readHeavy = loadModeArray(Load.READ_HEAVY);
    final static int[] writeHeavy = loadModeArray(Load.WRITE_HEAVY);
    final static int[] mixed = loadModeArray(Load.MIXED);
    final static int[] insertions = loadModeArray(Load.INSERTION);

    final static int[] deletions = loadModeArray(Load.DELETION);

    final static int serialPoolSize = 1;
    final static int serialMaxPoolSize = 1;
    final static long keepAliveTime = 1000; // todo change this.
    final static TimeUnit timeUnit = TimeUnit.MILLISECONDS; // TODO CHANGE THIS

    private static int getLoadNumber(Load load) {
        if (load == Load.WRITE_HEAVY)
            return 0;
        else if (load == Load.READ_HEAVY)
            return 1;
        else if (load == Load.MIXED)
            return 2;
        else if (load == Load.INSERTION)
            return 3;
        else return 4;
    }

    private static int getTreeIndex(int size) {
        for (int i = 0; i < initial_tree_size.length; i++) {
            if (initial_tree_size[i] == size)
                return i;
        }
        return 0;
    }

    private static int getThreadIndex(int[] threads, int threadNumber) {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == threadNumber)
                return i;
        }
        return 0;
    }

    public static void main(String[] args) {

        System.out.println("Starting...");

        var processors = Runtime.getRuntime().availableProcessors();
        var nelements = (int) (Math.log(processors)/Math.log(2)) + 1;

        int[] nThreads = new int[nelements];
        nThreads[0] = 1;
        for (int i = 1; i < nelements; i++) {
            nThreads[i] = nThreads[i-1] * 2;
        }


        // array to store all the execution times
        double [][][] serialBSTTimes = new double[initial_tree_size.length]
                                                [Load.values().length]
                                                [n_iterations];

        double[][][][] lockBSTTimes = new double[nThreads.length]
                                                [initial_tree_size.length]
                                                [Load.values().length]
                                                [n_iterations];

        double [][][][] lfBSTTimes = new double[nThreads.length]
                                                [initial_tree_size.length]
                                                [Load.values().length]
                                                [n_iterations];

        //int loadNumber;


        for (Integer threads: nThreads) {

            var threadIndex = getThreadIndex(nThreads, threads);

            for (Integer tree_size : initial_tree_size) {

                var treeIndex = getTreeIndex(tree_size);

                for (Load load : Load.values()) {

                    var loadNumber = getLoadNumber(load);

                    for (int iter = 0; iter < n_iterations; iter++) {

                        BST<Integer> lockBST = new LockBST<>(tree_size/2);
                        BST<Integer> serialBST = new SerialBST<>(tree_size / 2);
                        BST<Integer> lfBST = new LockFreeBST<>(tree_size + 3, tree_size + 2, tree_size + 1);

                        populateTree(tree_size, serialBST, lfBST, lockBST);

                        IntStream randomNumbers = ThreadLocalRandom.current().ints(n_operations, 1, tree_size); // TODO; add range and size.
                        List<Integer> allRandomNumbers  = new ArrayList<>(n_operations);
                        randomNumbers.forEach(allRandomNumbers::add);

                        if (threads == 1) {
                            //IntStream randomNumbers2 = ThreadLocalRandom.current().ints(n_operations, 1, tree_size);
                            List<Runnable> serialTasks = getTasks(serialBST, allRandomNumbers.iterator(), load);
                            LinkedBlockingQueue<Runnable> serialWorkQueue = new LinkedBlockingQueue<>(serialTasks);
                            BSTThreadPool serialPool = new BSTThreadPool(1, 1, keepAliveTime, timeUnit, serialWorkQueue);
                            long startTimeSerial = System.nanoTime();
                            serialPool.prestartAllCoreThreads();
                            serialPool.shutdown();
                            while (!serialPool.isTerminated()) ;
                            long endTimeSerial = System.nanoTime();
                            var durationSerial = (endTimeSerial-startTimeSerial)/Math.pow(10,9);
                            serialBSTTimes[treeIndex][loadNumber][iter] = durationSerial;
                            String printStatementSerial = "Serial- Tree size: "+ tree_size+",\t Iteration: "+iter + ",\t Execution: "
                                    + durationSerial;

                            System.out.println(printStatementSerial);
                        }

                        // lock version
                        List<Runnable> lockTasks = getTasks(lockBST, allRandomNumbers.iterator(), load);
                        LinkedBlockingQueue<Runnable> lockWorkQueue = new LinkedBlockingQueue<>(lockTasks);
                        BSTThreadPool lockPool = new BSTThreadPool(threads,threads,
                                                        keepAliveTime, timeUnit, lockWorkQueue);

                        lockPool.prestartAllCoreThreads();
                        long startTimeLock = System.nanoTime();
                        lockPool.shutdown();

                        while(!lockPool.isTerminated());

                        long endTimeLock = System.nanoTime();
                        var durationLock = (endTimeLock -startTimeLock)/Math.pow(10,9);
                        lockBSTTimes[threadIndex][treeIndex][loadNumber][iter] = durationLock;

                        String printStatementLock = "Lock - Threads: " + threads + "\t. Initial Tree Size: " + tree_size +
                                ".\t Load: " + load + ".\t Iteration: " + iter +
                                "\t Execution time: "  + durationLock + " seconds";
                        System.out.println(printStatementLock);

                        // lock free version
                        List<Runnable> lfTasks = getTasks(lfBST, allRandomNumbers.iterator(), load);
                        LinkedBlockingQueue<Runnable> lfWorkQueue = new LinkedBlockingQueue<>(lfTasks);

                        //System.out.println("Starting tasks.... in iteration: " +iter+ "\n");
                        BSTThreadPool lfPool = new BSTThreadPool(threads, threads, keepAliveTime, timeUnit, lfWorkQueue);


                        lfPool.prestartAllCoreThreads();
                        long startTimeLF = System.nanoTime();

                        lfPool.shutdown();

                        while(!lfPool.isTerminated());

                        long endTimeLF = System.nanoTime();
                        var durationLF = (endTimeLF - startTimeLF)/Math.pow(10,9);
                        lfBSTTimes[threadIndex][treeIndex][loadNumber][iter] = durationLF;

                        String printStatementLF = "LFBST - Threads: " + threads + ".\t Initial Tree Size: " + tree_size +
                                ".\t Load: " + load + ".\t Iteration: " + iter +
                                "\t Execution time: "  + durationLF + " seconds";

                        System.out.println(printStatementLF);
                    }
                }
            }
        }

        System.out.println("All Executions Completed..");
        serializeArray3d(serialBSTTimes, "/home/nandhan/serialBST.ser");
        serializeArray(lfBSTTimes, "/home/nandhan/LockFreeBST.ser");
        serializeArray(lockBSTTimes, "/home/nandhan/LockBST.ser"); // TODO save other arrays with different file name.

    }

    private static void serializeArray3d(double[][][] array, String fileName) {

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(array);
        }
        catch (Exception e){
            System.out.println("Exception occured");
        }
    }

    private static void serializeArray(double[][][][] array, String fileName){
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(array);
        }
        catch (Exception e){
            System.out.println("Exception occured");
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
    }

    private static int[] loadModeArray(Load load) {

        int[] type = new int[100];
        int insertStart, insertEnd, searchStart, searchEnd, deleteStart, deleteEnd;


        // VERIFY INDICES.
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
        else if (load == Load.MIXED) {
            insertStart = 0;
            insertEnd = 20;
            searchStart = 20;
            searchEnd = 90;
            deleteStart = 90;
            deleteEnd = 100;
        }
        else if (load == Load.INSERTION) {
            insertStart = 0;
            insertEnd = searchStart = searchEnd = deleteStart = deleteEnd = 100;
        }
        else { // load == DELETION
            insertStart = insertEnd = searchStart = searchEnd = deleteStart = 0;
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

    private static List<Runnable> getTasks(BST<Integer> bst, Iterator<Integer> randomIterator, Load load) {

        List<Runnable> tasks = new ArrayList<>(n_operations);
        // get type array
        int [] type;
        if (load == Load.MIXED)
            type = mixed;
        else if (load == Load.WRITE_HEAVY)
            type = writeHeavy;
        else if (load == Load.INSERTION)
            type = insertions;
        else if (load == Load.DELETION)
            type = deletions;
        else type = readHeavy;

        IntStream numbers = ThreadLocalRandom.current().ints(n_operations, 1,100);
        /*
        1 - insert
        2 - search
        3 - delete
         */

        numbers.forEach( (i) -> {//

            if (randomIterator.hasNext()) {

                int nextNumber = randomIterator.next();
                if (type[i] == 1)
                    tasks.add(() -> bst.insert(nextNumber));
                else if (type[i] == 2)
                    tasks.add(() -> bst.search(nextNumber));
                else if (type[i] == 3)
                    tasks.add(() -> bst.delete(nextNumber));
            }
        });

        return tasks;
    }
}

enum Load {
    WRITE_HEAVY,
    READ_HEAVY,
    MIXED,
    INSERTION,
    DELETION
}
