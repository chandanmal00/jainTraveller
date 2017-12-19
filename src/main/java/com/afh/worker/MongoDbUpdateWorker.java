package com.afh.worker;

import com.afh.controller.AskForHelpDAO;
import com.mongodb.Mongo;

/**
 * Created by chandan on 9/23/2015.
 */
public class MongoDbUpdateWorker implements Runnable{
    private String permalink ;
    private String username;

    public MongoDbUpdateWorker(String permalink, String username) {
        this.permalink = permalink;
        this.username = username;

    }

    @Override
    public void run() {
        AskForHelpDAO askForHelpDAO = new AskForHelpDAO();
        askForHelpDAO.upvoteNew(this.permalink,this.username);
    }
}

