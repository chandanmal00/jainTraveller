package com.afh.worker;

import com.afh.controller.AskForHelpDAO;
import com.afh.controller.Comments;

/**
 * Created by chandan on 9/23/2015.
 */
public class MongoDbUpdateCommentWorker implements Runnable{
    private String permalink ;
    private String username;
    private Comments comment;

    public MongoDbUpdateCommentWorker(String permalink, String username,Comments comment) {
        this.permalink = permalink;
        this.username = username;
        this.comment = comment;


    }

    @Override
    public void run() {
        AskForHelpDAO askForHelpDAO = new AskForHelpDAO();
        askForHelpDAO.addComment(this.permalink, this.username,this.comment);
    }
}

