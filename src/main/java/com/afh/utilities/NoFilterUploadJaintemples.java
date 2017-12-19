package com.afh.utilities;

/**
 * Created by chandan on 9/11/2015.
 * //ONly does Elastic Search insert
 * //Add Mongo too later
 */

import com.afh.controller.AskForHelpObject;
import com.afh.controller.ElasticSearchDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import persistence.ElasticCacheConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chandan on 9/11/2015.
 */
public class NoFilterUploadJaintemples {

    public static final String DIR = "D:\\test\\";
    public static final String FILE_NAME = "c_dump_out";

    public static void main(String[] args) {
        int count=0;
        int dCount=0;
        String line=null;
        FileReader fileReader;
        BufferedReader br = null;
        Map<String,Integer> dedupeMap = new HashMap<String,Integer>();
        try {


            fileReader = new FileReader(DIR + "/" + FILE_NAME);
            br = new BufferedReader(fileReader);


            List<String> list =new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                //System.out.println("HERE::"+line);
                if(line.length()>10) {
                    ObjectMapper om = new ObjectMapper();
                    Doctor doc = om.readValue(line, Doctor.class);
                    doc.setVoters(list);


                    AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                            .city(doc.city)
                            .country(doc.country)
                            .state(doc.state)
                            .type(doc.type)
                            .skills(doc.skills)
                            .name(doc.name)
                            .permalink(doc.permalink)
                            .voters(doc.voters)
                            .suggest(ElasticSearchSuggestDataCreator.getSuggestStructureForElasticSearchIndex(doc))
                            .build();


                    ElasticSearchDAO.putJson(askForHelpObject);


                    System.out.println(om.writeValueAsString(askForHelpObject));
                    // System.out.println(line);


                    //System.out.println(doc.getAddress());
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
        System.out.println("Count:"+count);
        System.out.println("Count:" + dCount);
        ElasticCacheConnection.shutDown();



    }
}
