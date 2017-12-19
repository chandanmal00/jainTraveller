package com.afh.controller;

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
public class AskFriendsObject {
    static final Logger logger = LoggerFactory.getLogger(AskFriendsObject.class);

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
    private List<Map<String, Object>> comments;
    private List<Map<String, Object>> tips;
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
        private List<Map<String, Object>> comments;
        private List<Map<String, Object>> tips;

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

        public AskForHelpObjectBuilder comments(List<Map<String, Object>> comments) {
            this.comments = comments;
            return this;
        }

        public AskForHelpObjectBuilder tips(List<Map<String, Object>> tips) {
            this.tips = tips;
            return this;
        }

        public AskFriendsObject build() {
            return new AskFriendsObject(this);
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

    //private constructor to enforce object creation through askForHelpObjectBuilder
    private AskFriendsObject(AskForHelpObjectBuilder askForHelpObjectBuilder) {
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
    }

    public static void main(String args[]) {
        //Creating object using AskForHelpObjectBuilder pattern in java
        AskFriendsObject whiteCake = new AskForHelpObjectBuilder().city("palo alto")
                .type("Doctor")
                .build();
        logger.info("{}",whiteCake);
    }

}

