/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.afh.controller;

import com.afh.constants.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.in;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.ElasticCacheConnection;
import persistence.MongoConnection;

public class AskForHelpDAO {
    MongoCollection<Document> listingCollection;
    MongoCollection<Document> getHelpCollection;
    MongoCollection<Document> listingCollectionReport;
    Client client;
    static final Logger logger = LoggerFactory.getLogger(AskForHelpDAO.class);


    public AskForHelpDAO() {
        this.client = ElasticCacheConnection.getInstance();
        listingCollection = MongoConnection.getInstance().getCollection(Constants.MONGO_LISTING_COLLECTION);
        listingCollectionReport  = MongoConnection.getInstance().getCollection(Constants.MONGO_LISTING_COLLECTION_REPORT);
        getHelpCollection= MongoConnection.getInstance().getCollection(Constants.MONGO_GET_HELP_COLLECTION);
    }

    public Document findByPermalink(String permalink) {
        Document permalinkDoc = new Document("permalink", permalink);
        logger.info("Trying to find by perm:" + permalink + "::" + permalinkDoc.toJson());
        Document askForHelpDocument = listingCollection.find(permalinkDoc).first();
        return askForHelpDocument;
    }

    public Document upvote(String permalink,String username) {

        logger.info("Trying to update by perm:"+permalink + ", username:"+username);
        if(username!=null) {


            //  Document askForHelpDocument = listingCollection.find(new Document("permalink", permalink)).first();

            try {
                UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                        new Document("$addToSet", new Document("voters", username)));
                //.append("dateUpdated", new Date()));
                ElasticSearchDAO.update(permalink, username);

                logger.info("Matches: " + result.getMatchedCount());
                logger.info("Modified: " + result.getModifiedCount());
            } catch(Exception e) {
                logger.error("Error updating the document, permalink"+permalink);

            }
        }
        //Two things
        //user, vote, dateUpdatedBy
        Document askForHelpDocument = listingCollection.find(new Document("permalink", permalink)).first();
        logger.info("Inserted askForHelp doc with permalink " + permalink);

        return askForHelpDocument;
    }

    public void upvoteNew(String permalink,String username) {

        logger.info("Trying to update by perm:"+permalink + ", username:"+username);
        if(username!=null) {
            try {
                UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                        new Document("$addToSet", new Document("voters", username)));
            } catch(Exception e) {
                logger.error("Error updating the document, permalink: {}",permalink,e);
                return;
               // throw new Exception("Error updating permalink:"+permalink+", user:"+username);
            }
        }
        return;

    }


    public Document addComment(String permalink,String username, Comments comment) {

        logger.info("Trying to addComment for perm:"+permalink + ", username:"+username);
        if(username!=null) {

            //  Document askForHelpDocument = listingCollection.find(new Document("permalink", permalink)).first();

            try {
                Gson gson = new Gson();
                Document commentDoc = new Document("username",comment.getUsername())
                        .append("email",comment.getEmail())
                        .append("comment",comment.getComments());
                UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                        new Document("$push", new Document("comments", commentDoc)));
                //.append("dateUpdated", new Date()));
                //Not doing ElasticSearchUpdate
                /*
                ElasticSearchDAO.update(client, permalink, username);

                logger.info("Matches: " + result.getMatchedCount());
                logger.info("Modified: " + result.getModifiedCount());
                */
            } catch(Exception e) {
                logger.error("Error updating the comments for document, permalink"+permalink);

            }
        }
        //Two things
        //user, vote, dateUpdatedBy
        Document askForHelpDocument = listingCollection.find(new Document("permalink", permalink)).first();
        logger.info("Inserted askForHelp doc with permalink " + permalink);

        return askForHelpDocument;
    }


    public List<Document> findByDateDescending(int limit) {

        List<Document> askForHelpDocuments = listingCollection.find().sort(new Document("dateUpdated", -1)).limit(limit).into(new ArrayList<Document>());

        return askForHelpDocuments;
    }

    /*
    public List<Document> findByTagDateDescending(final String tag) {

//        BasicDBObject query = new BasicDBObject("tags", tag);
        Bson filter = in("tags",tag);

        //System.out.println("/tag query: " + filter.toBsonDocument(Document.class,new Co).toJson());
        List<Document> posts = listingCollection.find(filter).sort(new Document("date", -1))
                .limit(10).into(new ArrayList<Document>());
        System.out.println("For tag: "+tag);

        return posts;
    }
    */

    public String addAskForHelpDocument(String type, List<String> skills, String name, String notes, String city, String state, String country, String username) {

       logger.info("inserting askForhelp entry " + type + " " + notes + ":" + city + ":" + name);
        String[] permalinkArr = new String[]{type,skills.get(0),name,city,state,country};

        String permalink = StringUtils.join(permalinkArr, '_');
        permalink = permalink.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        String permLinkExtra = String.valueOf(GregorianCalendar
                .getInstance().getTimeInMillis());
        permalink += permLinkExtra;


        Document askForHelpDocument = new Document("type", type);
        //Is an array so need to append TODO
        askForHelpDocument.append("skills", skills);
        askForHelpDocument.append("name", name);
        //Is an array so need to append TODO
        List<String> notesList = new ArrayList<String>();
        notesList.add(notes);
        askForHelpDocument.append("notes", notesList);
        askForHelpDocument.append("permalink", permalink);

        //append user
        List<String> users = new ArrayList<String>();
        users.add(username);
        askForHelpDocument.append("voters", users);

        //Add city, country, state, zipcode
        askForHelpDocument.append("city", city);
        askForHelpDocument.append("state", state);
        askForHelpDocument.append("country", country);
        //askForHelpDocument.append("zipcode", );

        askForHelpDocument.append("dateCreated", new Date());
        askForHelpDocument.append("dateUpdated", new Date());

        try {
            listingCollection.insertOne(askForHelpDocument);
            AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                    .type(type)
                    .name(name)
                    .permalink(permalink)
                    .city(city)
                    .state(state)
                    .country(country)
                    .skills(skills)
                    .voters(users)
                    .build();
            logger.info("{}",askForHelpObject);

            ElasticSearchDAO.putJson(askForHelpObject);

            logger.info("Inserted askForHelp doc with permalink " + permalink);
        } catch (Exception e) {
           logger.error("Error inserting post", e);
            return null;
        }

        return permalink;
    }

    /*
    public void addNote(final String name, final String email, final String body, final String permalink) {
        Document comment = new Document("author", name)
                .append("body", body);
        if (email != null && !email.equals("")) {
            comment.append("email", email);
        }

       UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                new Document("$push",
                        new Document("comments", comment)));

        System.out.println("Matches: " +result.getMatchedCount());
        System.out.println("Modified: " + result.getModifiedCount());
    }
    */
    public static void main(String[] args) {
        /*
        final MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost"));
        final MongoDatabase askForHelpDataBase = mongoClient.getDatabase("blog");
        */
        AskForHelpDAO askForHelpDAO = new AskForHelpDAO();
        Document doc = askForHelpDAO.findByPermalink("doctor_pediatrician_paul_protter_sunnyvale_us1441949238302");
        if(doc==null) {
            logger.info("Dco is null");
        } else {
            logger.info(doc.toJson());
        }


    }

    public Boolean updateListingWithJainFoodOption(String permalink, String username) {
        if(username==null || permalink==null) {
            logger.error("Null Inputs for method:updateListingWithJainFoodOption");
            return false;
        }
        try {
            Document updateDoc = new Document("$addToSet", new Document("jainVoters", username)).append("$set", new Document("lastJainUpdateDateTime", new Date()));
            logger.info(updateDoc.toJson());
            UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                    updateDoc);
            return true;
        }catch(Exception e) {
            logger.error("Error while updating DB for jain Flag for inputs: {}, {}",permalink,username,e);

        }
        return false;


    }

    public Boolean updateListingWithPhotos(String permalink, String username,String imageName) {
        if(username==null || permalink==null) {
            logger.error("Null Inputs for method:updateListingWithPhotos");
            return false;
        }
        try {
            Document newImage = new Document("username",username)
                    .append("imageName", imageName);
            UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                    new Document("$push", new Document("userImages", newImage)));
            return true;
        }catch(Exception e) {
            logger.error("Error while updating DB for jain Flag for inputs: {}, {}",permalink,username,e);

        }
        return false;


    }

    public void addGetHelpListing(String username, List<String> toAddress, AskForHelpObject askForHelpObject) {
        logger.info("Adding Get help listing");
        try {
            Document askForHelpDocument = new Document("username", username);
            //Is an array so need to append TODO
            Gson gson = new Gson();
            askForHelpDocument.append("getHelpDoc", gson.toJson(askForHelpObject));
            askForHelpDocument.append("emailList", toAddress);
            askForHelpDocument.append("creationDate", new Date());
            getHelpCollection.insertOne(askForHelpDocument);
            logger.info("Inserted Get help for username:{} ",username);
        } catch (Exception e) {
            logger.error("Error inserting post", e);
            return;
        }
    }

    public void updateListingWithInputJson(String permalink, JsonObject jsonObject) {

        if(permalink==null || jsonObject==null) {
            logger.error("Null Inputs for method:updateListingWithInputJson");
            return;
        }
        logger.info("updating permalin:{}",permalink);
        try {

            Document newJsonUpdte = new Document();
            final Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            Gson gson = new Gson();
            for(Map.Entry<String,JsonElement> entry : entries) {
                String value = gson.toJson(entry.getValue());
                logger.info(gson.toJson(entry.getValue().toString()));
                try {
                    //Try if its a number
                    newJsonUpdte.append("$set", new Document(StringEscapeUtils.escapeHtml4(entry.getKey()),
                            Integer.parseInt(value.replaceAll("^\"|\"$", ""))));
                } catch(Exception e) {
                    newJsonUpdte.append("$set", new Document(StringEscapeUtils.escapeHtml4(entry.getKey()),
                            StringEscapeUtils.escapeHtml4(value.replaceAll("^\"|\"$", ""))));
                }
            }

            logger.info("ToUpdateJson:" + gson.toJson(newJsonUpdte));

            UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                    newJsonUpdte);
            logger.info("Success:{}", result.toString());
            //append("$set", new Document("lastJainUpdateDateTime", new Date()));
              //      new Document("$push", new Document("userImages", newImage)));
            return;
        } catch(Exception e) {
            logger.error("Error while updating DB for jain Flag for inputs: {}, {}",permalink,e);

        }
        return;
    }

    public void deleteListing(String permalink) {

        if(permalink==null || permalink.isEmpty()) {
            logger.error("Null Inputs for method:deleteListing, input:{}",permalink);
            return;
        }
        logger.info("deleting permalink:{}",permalink);
        try {

            DeleteResult deleteResult = listingCollection.deleteOne(new Document("permalink", permalink));
            logger.info(deleteResult.toString());
            return;
        } catch(Exception e) {
            logger.error("Error while deleting DB for permalink: {}, {}",permalink,e);

        }
        return;
    }

    public void reportListing(String permalink, String username, String reason) {
        if( username==null
                || permalink==null
                || reason==null|| StringUtils.isBlank(permalink)|| StringUtils.isBlank(reason)        ) {
            logger.error("Null or empty Inputs for method:reportListing");
            return;
        }
        reason = StringEscapeUtils.escapeHtml4(reason);
        try {
            Document newReport = new Document("username",username)
                    .append("reason", reason)
                    .append("CreationDate", new Date());

            Document newReport_clone = Document.parse(newReport.toJson());
            listingCollectionReport.insertOne(newReport_clone.append("permalink", permalink));
            UpdateResult result = listingCollection.updateOne(new Document("permalink", permalink),
                    new Document("$push", new Document("report", newReport)));
            logger.info("Successfully Reported {} with reason:{}",permalink,reason);
            return;
        }catch(Exception e) {
            logger.error("Error while reportListing for inputs: {}, {}",permalink,username,e);
        }
        return;
    }
}
