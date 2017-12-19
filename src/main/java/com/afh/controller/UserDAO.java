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
import com.afh.utilities.ControllerUtilities;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.MongoConnection;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class UserDAO {
    private final MongoCollection<Document> usersCollection;
    private final MongoCollection<Document> usersResetCollection;
    private final MongoCollection<Document> usersProfileCollection;
    private final MongoCollection<Document> usersProfileHistoryCollection;
    private Random random = new SecureRandom();
    static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public UserDAO() {
        this.usersCollection= MongoConnection.getInstance().getCollection(Constants.MONGO_USERS_COLLECTION);
        this.usersResetCollection= MongoConnection.getInstance().getCollection(Constants.MONGO_USERS_RESET_COLLECTION);
        this.usersProfileCollection = MongoConnection.getInstance().getCollection(Constants.MONGO_USERS_PROFILE_COLLECTION);
        this.usersProfileHistoryCollection = MongoConnection.getInstance().getCollection(Constants.MONGO_USERS_PROFILE_HISTORY_COLLECTION);
    }

    // validates that username is unique and insert into db
    public boolean addUser(String username, String password, String email) {

        String passwordHash = makePasswordHash(password, Integer.toString(random.nextInt()));

        Document user = new Document();

        user.append("_id", username).append("password", passwordHash);

        if (email != null && !email.equals("")) {
            // the provided email address
            user.append("email", email);
        }

        try {
            usersCollection.insertOne(user);
            return true;
        } catch (MongoWriteException e) {
            logger.error("Username already in use: {}", username, e);
            return false;
        }
    }

    // validates that username is unique and insert into db
    public boolean addUser(Map<String,String> rootMap) {

        String password = rootMap.get("password");
        String firstname = rootMap.get("firstname");
        String lastname = rootMap.get("lastname");
        String email = rootMap.get("email");
        String phonenumber = rootMap.get("phonenumber");
        String username = rootMap.get("username");

        String passwordHash = makePasswordHash(password, Integer.toString(random.nextInt()));

        Document user = new Document();

        user.append("_id", username).append("password", passwordHash);

        if (email != null && !email.equals("")) {
            // the provided email address
            user.append("email", email);
        }
        user.append("firstname",firstname)
                .append("lastname",lastname)
                .append("phonenumber",phonenumber);

        try {
            usersCollection.insertOne(user);
            return true;
        } catch (MongoWriteException e) {
            logger.error("Username already in use: {}", username, e);
            return false;
        }
    }

    private Boolean checkIfProfileChanged(Document userProfileDoc, UserProfile userProfile) {
        if(userProfileDoc==null) {
            return false;
        }

        try {
            UserProfile dbUserProfile = new UserProfile();
            dbUserProfile.setAddress(userProfileDoc.getString("address"));
            dbUserProfile.setCountry(userProfileDoc.getString("country"));
            dbUserProfile.setCity(userProfileDoc.getString("city"));
            dbUserProfile.setState(userProfileDoc.getString("state"));
            dbUserProfile.setMobile(userProfileDoc.getString("mobile"));
            dbUserProfile.setFirstname(userProfileDoc.getString("firstname"));
            dbUserProfile.setLastname(userProfileDoc.getString("lastname"));
            dbUserProfile.setProfilephoto(userProfileDoc.getString("profilephoto"));
            dbUserProfile.setEmail(userProfileDoc.getString("email"));

            if (userProfile.equals(dbUserProfile)) {
                return true;
            }
        } catch(Exception e) {
            logger.error("Some problem with the inputs userProfile:{} , userProfileDocument from mongo:{}"
                    ,userProfile,userProfileDoc);
        }
        return false;
    }

    public Document addUserProfile(String username, UserProfile userProfile) {

        try {
            //Find if User has a profile

            Document userToFind = new Document("_id", username);
            Document userProfileDoc = usersProfileCollection.find(userToFind).first();
            if(checkIfProfileChanged(userProfileDoc,userProfile)) {
                logger.warn("Profiles are same, so not updating");
                return userProfileDoc;
            }

            Document newUserProfileDoc = new Document("email", userProfile.getEmail())
                    .append("firstname", userProfile.getFirstname())
                    .append("lastname", userProfile.getLastname())
                    .append("mobile",userProfile.getMobile())
                    .append("address", userProfile.getAddress())
                    .append("city",userProfile.getCity())
                    .append("state", userProfile.getState())
                    .append("country",userProfile.getCountry())
                    .append("profilephoto", userProfile.getProfilephoto())
                    .append("dateCreated", new Date());

            if(userProfileDoc==null) {
                logger.info("ProfileUpdate for first time for user:"+username);
                newUserProfileDoc = newUserProfileDoc.append("_id", username);
                usersProfileCollection.insertOne(newUserProfileDoc);
                return newUserProfileDoc;
            } else {
                //We need to update
                //Basically we are deleting
                //adding to history
                //inserting again to original table
                //newUserProfileDoc = newUserProfileDoc.append("dateCreated",userProfileDoc.getDate("dateCreated"));
                logger.info("updating {} with {}:", userProfileDoc.toJson(), newUserProfileDoc.toJson());

                usersProfileCollection.deleteOne(userToFind);
                userProfileDoc.remove("_id");
                usersProfileHistoryCollection.insertOne(userProfileDoc.append("username",username));

                newUserProfileDoc = newUserProfileDoc.append("_id", username);
                usersProfileCollection.insertOne(newUserProfileDoc);
                return newUserProfileDoc;

            }
        } catch (MongoWriteException e) {
            logger.error("Issue in adding userProfile: {}", username, e);
            return null;
        }
    }

    public Document getUserProfile(String username) {


        try {
            //Find if User has a profile
            Document userToFind = new Document("_id", username);
            Document userProfileDoc = usersProfileCollection.find(userToFind).first();
            return userProfileDoc;

        } catch (MongoWriteException e) {
            logger.error("Issue in getting userProfile: {}", username, e);
        }
        return null;
    }


    public Document validateLogin(String username, String password) {


        Document user = usersCollection.find(new Document("_id", username)).first();

        if (user == null) {
            System.out.println("User not in database");
            return null;
        }

        String hashedAndSalted = user.get("password").toString();

        String salt = hashedAndSalted.split(",")[1];

        if (!hashedAndSalted.equals(makePasswordHash(password, salt))) {
            System.out.println("Submitted password is not a match");
            return null;
        }

        return user;
    }


    public boolean addResetUserRequest(String email) {
        Document user = new Document();
        user.append("email", email).append("dateCreated", new Date());
        if (email==null ||
                StringUtils.isBlank(email) ||
                !ControllerUtilities.emailVerify(email)) {
            // the provided email address
            logger.info("Failed email checks, input:{}",email);
            return false;
        }

        try {
            usersResetCollection.insertOne(user);
            logger.info("Added email for reset, input:{}",email);
            return true;
        } catch (MongoWriteException e) {
            logger.error("Some error in adding reset for email: {}", email, e);
            return false;
        }
    }

    private String makePasswordHash(String password, String salt) {
        try {
            String saltedAndHashed = password + "," + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(saltedAndHashed.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            byte hashedBytes[] = (new String(digest.digest(), "UTF-8")).getBytes();
            return encoder.encode(hashedBytes) + "," + salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unavailable?  Not a chance", e);
        }
    }
}
