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
import com.afh.utilities.StateUS;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.MongoConnection;
import sun.misc.BASE64Encoder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class StateDAO {
    private final MongoCollection<Document> stateCollection;
    static final Logger logger = LoggerFactory.getLogger(StateDAO.class);
    public StateDAO() {
        stateCollection = MongoConnection.getInstance().getCollection(Constants.MONGO_STATE_COLLECTION);
    }


    // starts a new session in the sessions table
    public void insertState(StateUS state) {

        Document stateDoc = new Document();

        stateDoc.append("value", state.getState()).append("stateShort", state.getStateShort());

        try {
            stateCollection.insertOne(stateDoc);

        } catch (MongoWriteException e) {
            logger.error("State already in use: " + state, e);
        }
    }

    public List<Document> getAllStates() {

        List<Document> stateDocs = stateCollection.find()
                .projection(new Document("_id",0)
                  .append("value",1)
                  .append("stateShort",1)
                )
                .into(new ArrayList<Document>());
        return stateDocs;
    }

}
