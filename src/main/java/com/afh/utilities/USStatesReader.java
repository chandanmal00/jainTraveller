package com.afh.utilities;

/**
 * Created by chandan on 9/11/2015.
 * Writes to MongoDb from the input CSV
 */

import com.afh.constants.Constants;
import com.afh.controller.StateDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chandan on 9/11/2015.
 */
public class USStatesReader {

    public static final String DIR = "D:\\Doctors\\States";
    public static final String FILE_NAME = "States_US.csv";
    static final Logger logger = LoggerFactory.getLogger(USStatesReader.class);
    StateDAO stateDao = null;

    public USStatesReader() {
        this.stateDao = new StateDAO();

    }

    public StateDAO getStateDao() {
        return stateDao;
    }

    public void setStateDao(StateDAO stateDao) {
        this.stateDao = stateDao;
    }

    public static void main(String[] args) {
        int count=0;
        int dCount=0;
        String line=null;
        FileReader fileReader;
        BufferedReader br = null;
        Map<String,Integer> dedupeMap = new HashMap<String,Integer>();
        USStatesReader usStatesReader = new USStatesReader();

        try {

            fileReader = new FileReader(DIR + "/" + FILE_NAME);
            br = new BufferedReader(fileReader);



            while ((line = br.readLine()) != null) {
                //System.out.println("HERE::"+line);
                if(line.length()>3) {
                    String[] stateLine = line.split(",");
                    StateUS state = new StateUS();
                    state.setState(stateLine[0]);
                    state.setStateShort(stateLine[1]);
                    ObjectMapper om = new ObjectMapper();
                    System.out.println(om.writeValueAsString(state)+",");
                    usStatesReader.getStateDao().insertState(state);
                    count++;
                }
            }
        } catch(Exception e) {
            System.out.println("Count:"+count + ",line:"+line);
            e.printStackTrace();
        } finally {
            try {
                br.close();
            }catch(Exception e) {

            }
        }
        System.out.println("Count:" + count);

        List<Document> stateDocs = usStatesReader.getStateDao().getAllStates();
        for(Document state: stateDocs) {
            logger.info(state.toJson());
        }

    }
}
