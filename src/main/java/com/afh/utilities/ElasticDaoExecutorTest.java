package com.afh.utilities;

import com.afh.controller.ElasticSearchWorkerDAO;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chandan on 9/19/2015.
 */
public class ElasticDaoExecutorTest {

    public static void main(String[] args) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, // core size
                50, // max size
                10*60, // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(20)); // queue with a size

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        for(int i=0;i<1000;i++) {
            Runnable elasticSearchWorkedDAO = new ElasticSearchWorkerDAO(null,null);
            threadPoolExecutor.execute(elasticSearchWorkedDAO);
        }
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.isTerminated()) {
        }
        System.out.println("Finished all threads");


    }
}
