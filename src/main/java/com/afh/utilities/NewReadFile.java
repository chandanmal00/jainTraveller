package com.afh.utilities;

import com.afh.controller.AskForHelpObject;
import com.afh.controller.ElasticSearchDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import persistence.ElasticCacheConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by chandan on 9/11/2015.
 */
public class NewReadFile {

    public static final String DIR = "D:\\Doctors\\";
    public static final String FILE_NAME = "data.txt";

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



            while ((line = br.readLine()) != null) {
                //System.out.println("HERE::"+line);
                if(line.length()>10) {
                    ObjectMapper om = new ObjectMapper();
                    Doctor doc = om.readValue(line, Doctor.class);


                        if (doc.skills != null) {

                            String[] permalinkArr = new String[]{doc.type,doc.skills.get(0),doc.name,doc.city,doc.state,doc.country};

                            String permalink = StringUtils.join(permalinkArr, '_');
                            permalink = permalink.replaceAll("\\s", "_"); // whitespace becomes _
                            permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
                            permalink = permalink.toLowerCase();

                            String permLinkExtra = String.valueOf(GregorianCalendar
                                    .getInstance().getTimeInMillis());
                            permalink += permLinkExtra;
                            doc.setPermalink(permalink);
                            if(doc.city==null) {

                                String address = doc.getAddress();
                                String[] addr = address.split(" ");
                                doc.setCity(StringUtils.trim(addr[addr.length-1]));
                                doc.state="CA";
                                doc.country="US";

                            }

                            AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                                    .city(doc.city)
                                    .country(doc.country)
                                    .state(doc.state)
                                    .type(doc.type)
                                    .skills(doc.skills)
                                    .name(doc.name)
                                    .permalink(doc.permalink)
                                    .voters(doc.voters)
                                    .build();

                            ElasticSearchDAO.putJson(askForHelpObject);


                            System.out.println(om.writeValueAsString(doc));
                            // System.out.println(line);
                            dCount++;
                        }
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
        System.out.println("Count:"+dCount);

        ElasticCacheConnection.shutDown();



    }
}
