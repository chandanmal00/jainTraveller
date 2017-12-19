package com.afh.utilities;

import com.afh.controller.AskForHelpObject;
import com.afh.model.ElasticSearchSuggest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chandan on 9/26/2015.
 */
public class ElasticSearchSuggestDataCreator {

    public static ElasticSearchSuggest getSuggestStructureForElasticSearchIndex(Doctor doc) {

        ElasticSearchSuggest elasticSearchSuggest = new ElasticSearchSuggest();


        List<String> inputs = new ArrayList<String>();
        inputs.add(doc.name);
        inputs.add(doc.city + " " + doc.state);
        inputs.addAll(doc.skills);
        elasticSearchSuggest.setInput(inputs);

        elasticSearchSuggest.setOutput(doc.name);
        Map<String,Object> payloadMap = new HashMap<String, Object>();
        payloadMap.put("permalink",doc.permalink);
        payloadMap.put("skills",doc.skills);
        payloadMap.put("city",doc.city);
        payloadMap.put("state",doc.state);
        payloadMap.put("country",doc.country);
        elasticSearchSuggest.setPayload(payloadMap);

        return elasticSearchSuggest;
    }

    public static ElasticSearchSuggest getSuggestStructureForElasticSearchIndex(AskForHelpObject askForHelpObject) {
        List<String> inputs = new ArrayList<String>();
        inputs.add(askForHelpObject.getName());
        inputs.add(askForHelpObject.getCity() + " " + askForHelpObject.getState());
        inputs.addAll(askForHelpObject.getSkills());
        ElasticSearchSuggest elasticSearchSuggest = new ElasticSearchSuggest();
        elasticSearchSuggest.setInput(inputs);
        elasticSearchSuggest.setOutput(askForHelpObject.getName());
        Map<String,Object> payloadMap = new HashMap<String, Object>();
        payloadMap.put("permalink", askForHelpObject.getPermalink());
        payloadMap.put("skills", askForHelpObject.getSkills());
        payloadMap.put("city", askForHelpObject.getCity());
        payloadMap.put("state",askForHelpObject.getState());
        payloadMap.put("country",askForHelpObject.getCountry());
        elasticSearchSuggest.setPayload(payloadMap);

        return elasticSearchSuggest;
    }
}
