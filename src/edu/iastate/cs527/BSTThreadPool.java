package edu.iastate.cs527;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class BSTThreadPool extends ThreadPoolExecutor {

    private final ThreadLocal<Long> start = new ThreadLocal<>();
    private final ThreadLocal<Long> end = new ThreadLocal<>();
    private final AtomicLong numTasks = new AtomicLong(0);
    public final AtomicLong totalTime = new AtomicLong();

    private final Logger logger = Logger.getLogger("BST Performance Logger");
    public BSTThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        // TODO need logging?
        //System.out.println("Starting Thread");
        logger.fine("Starting thread");
        start.set(System.nanoTime());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        end.set(System.nanoTime());
        logger.fine("Ending thread from thread pool");
        //System.out.println("Ending thread");
        long timeElapsed = end.get() - start.get();
        totalTime.addAndGet(timeElapsed);
        numTasks.incrementAndGet();
        super.afterExecute(r, t);
    }
}
