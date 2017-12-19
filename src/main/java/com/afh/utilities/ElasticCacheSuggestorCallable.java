package com.afh.utilities;

/**
 * Created by chandan on 9/19/2015.
 */


import com.afh.controller.ElasticSearchDAO;
import com.google.gson.JsonArray;
import persistence.ElasticCacheConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class ElasticCacheSuggestorCallable implements Callable<JsonArray> {

    private String query;

    public ElasticCacheSuggestorCallable(String query) {
        this.query = query;
    }

    @Override
    public JsonArray call() throws Exception {
        return ElasticSearchDAO.getSuggestions(query);
    }

    public static void main(String args[]){
        //Get ExecutorService from Executors utility class, thread pool size is 10
        final JsonArray ind = ElasticSearchDAO.getSuggestions("ja");
        if(ind!=null) {
            for (int k = 0; k < ind.size(); k++) {
                System.out.println(k + "::" + ind.get(k).toString());
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        //create a list to hold the Future object associated with Callable
        List<Future<JsonArray>> list = new ArrayList<Future<JsonArray>>();
        //Create MyCallable instance
        Callable<JsonArray> callable = new ElasticCacheSuggestorCallable("jain");
        for(int i=0; i<1; i++){
            //submit Callable tasks to be executed by thread pool
            Future<JsonArray> future = executor.submit(callable);
            System.out.println("HERE:"+i);
            //add Future to the list, we can get return value using Future
            list.add(future);
        }
        for(Future<JsonArray> fut : list){
            try {
                //print the return value of Future, notice the output delay in console
                // because Future.get() waits for task to get completed
                System.out.println(new Date()+ "::"+fut.get());
            }catch  (InterruptedException e) {
                e.printStackTrace();
            } catch(ExecutionException ez){
                ez.printStackTrace();
            }
        }
        //shut down the executor service now
        executor.shutdown();
    }

}