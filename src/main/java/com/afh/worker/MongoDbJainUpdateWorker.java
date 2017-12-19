package com.afh.worker;

import com.afh.controller.AskForHelpDAO;

/**
 * Created by chandan on 9/23/2015.
 */
public class MongoDbJainUpdateWorker implements Runnable{
    private String permalink ;
    private String username;

    public MongoDbJainUpdateWorker(String permalink, String username) {
        this.permalink = permalink;
        this.username = username;

    }

    @Override
    public void run() {
        AskForHelpDAO askForHelpDAO = new AskForHelpDAO();
        askForHelpDAO.updateListingWithJainFoodOption(this.permalink,this.username);
    }
}

