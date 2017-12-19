package com.afh.controller;

import com.afh.model.ElasticSearchSuggest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by chandan on 9/5/2015.
 */
public class AskForHelpObject {
    static final Logger logger = LoggerFactory.getLogger(AskForHelpObject.class);

    //Use cases
    //Recommend me a Pediatrician in Palo Alto, CA, US - Doctor
    //Recommend me a Place to buy Clothes - Shopping
    //Recommend me thingsToDo related ti SiteSeeing in SFO
    //Recommend me lipstick brand
    //Recommend me good place  to Buy Car Seat

    //private String title;
    private String type;
    private List<String> skills;

    //Listing Details
    private String name;
    private String permalink;
    private String city;
    private String state;
    private String country;

    //private String category;

    private List<String> voters;
    private List<Comments> comments;
    private ElasticSearchSuggest suggest;

    private List<Tips> tips;
    /*
    / "suggest" : {
    "input": [ "Nevermind", "Nirvana" ],
            "output": "Nirvana - Nevermind",
            "payload" : { "artistId" : 2321 },
            "weight" : 34
}
"suggest" : {
        "input": [ "Nevermind", "Nirvana" ],
        "output": "Nirvana - Nevermind",
        }
*/


    //private List<Map<String,Object>> Tags;

    public static class AskForHelpObjectBuilder {
        private String type;
        private List<String> skills;

        //Listing Details
        private String name;
        private String permalink;
        private String city;
        private String state;
        private String country;

        //private String category;

        private List<String> voters;
        private List<Comments> comments;
        private ElasticSearchSuggest suggest;
        private List<Tips> tips;

        public AskForHelpObjectBuilder type(String type) {
            this.type = type;
            return this;
        }

        public AskForHelpObjectBuilder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public AskForHelpObjectBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AskForHelpObjectBuilder permalink(String permalink) {
            this.permalink = permalink;
            return this;
        }

        public AskForHelpObjectBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AskForHelpObjectBuilder state(String state) {
            this.state = state;
            return this;
        }

        public AskForHelpObjectBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AskForHelpObjectBuilder voters(List<String> voters) {
            this.voters = voters;
            return this;
        }

        public AskForHelpObjectBuilder comments(List<Comments> comments) {
            this.comments = comments;
            return this;
        }

        public AskForHelpObjectBuilder suggest(ElasticSearchSuggest suggest) {
            this.suggest = suggest;
            return this;
        }

        public AskForHelpObjectBuilder tips(List<Tips> tips) {
            this.tips = tips;
            return this;
        }

        public AskForHelpObject build() {
            return new AskForHelpObject(this);
        }
    }

    @Override
    public String toString() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return om.writeValueAsString(this);
        }catch (Exception e) {
            logger.error("exception",e);
        }
        return "exception";
    }

    public String getCity() {
        return city;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public String getCountry() {
        return country;
    }

    public String getPermalink() {
        return permalink;
    }

    public List<String> getSkills() {
        return skills;
    }

    public String getState() {
        return state;
    }

    public ElasticSearchSuggest getSuggest() {
        return suggest;
    }

    public List<Tips> getTips() {
        return tips;
    }

    public String getType() {
        return type;
    }

    public List<String> getVoters() {
        return voters;
    }

    public String getName() {
        return name;
    }

    //private constructor to enforce object creation through askForHelpObjectBuilder
    private AskForHelpObject(AskForHelpObjectBuilder askForHelpObjectBuilder) {
        this.type = askForHelpObjectBuilder.type;
        this.name = askForHelpObjectBuilder.name;
        this.skills = askForHelpObjectBuilder.skills;
        this.city = askForHelpObjectBuilder.city;
        this.state = askForHelpObjectBuilder.state;
        this.country = askForHelpObjectBuilder.country;
        this.voters = askForHelpObjectBuilder.voters;
        this.comments = askForHelpObjectBuilder.comments;
        this.tips = askForHelpObjectBuilder.tips;
        this.permalink = askForHelpObjectBuilder.permalink;
        this.suggest = askForHelpObjectBuilder.suggest;
    }

    public static void main(String args[]) {
        //Creating object using AskForHelpObjectBuilder pattern in java
        AskForHelpObject whiteCake = new AskForHelpObjectBuilder().city("palo alto")
                .type("Doctor")
                .build();
        logger.info("{}",whiteCake);
    }

    public void setSuggest(ElasticSearchSuggest suggest) {
        this.suggest = suggest;
    }
}

