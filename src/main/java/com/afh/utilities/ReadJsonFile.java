package com.afh.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chandan on 9/11/2015.
 */
public class ReadJsonFile {

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

                    
                    String name = doc.name;
                    String[] names = name.split("\\.");
                    String newName = StringUtils.join(names, "", 1, names.length);
                    String key = newName;
                    doc.setName(newName);
                    doc.setType("Doctor");
                    doc.setVoters(Arrays.asList("test"));

                    if(dedupeMap.get(key)==null) {
                        dedupeMap.put(key, 1);

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
                            System.out.println(om.writeValueAsString(doc));
                            // System.out.println(line);
                            dCount++;
                        }
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



    }
}
