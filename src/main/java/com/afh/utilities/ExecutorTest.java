package com.afh.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by chandan on 9/19/2015.
 */
public class ExecutorTest {
    static final Logger logger = LoggerFactory.getLogger(ExecutorTest.class);
    public static void main(String[] args) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, // core size
                10, // max size
                10, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(20)); // queue with a size

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        for(int i=0;i<32;i++) {
            System.out.println("Starting Thread:" + i);
            Runnable worker = new Worker("" + i);
            threadPoolExecutor.execute(worker);
            logger.info("HERE:"+i);
        }
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.isTerminated()) {
        }
        System.out.println("Finished all threads");
        threadPoolExecutor.shutdownNow();


    }
}
