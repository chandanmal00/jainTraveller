package com.afh.controller;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chandan on 9/20/2015.
 */
public class UserProfile {
    static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private String firstname;
    private String lastname;
    private String email;
    private String mobile;
    private String address;
    private String city;
    private String state;
    private String country;
    private String profilephoto = "Penguins.jpg";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname.trim();
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile.trim();
    }

    public String getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(String profilephoto) {
        this.profilephoto = profilephoto.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state.trim();
    }

    @Override
    public boolean equals(Object obj) {
        //Implementing a basic equals for now, need to implement better one
        if(obj==this) return true;
        if(obj==null || obj.getClass()!=this.getClass()) {
            return false;
        }

        UserProfile otherUser = (UserProfile) obj;
        if(otherUser.getCity().equals(this.getCity())
                && otherUser.getAddress().equals(this.getAddress())
                && otherUser.getState().equals(this.getState())
                && otherUser.getCountry().equals(this.getCountry())
                && otherUser.getFirstname().equals(this.getFirstname())
                && otherUser.getLastname().equals(this.getLastname())
                && otherUser.getProfilephoto().equals(this.getProfilephoto())
                && otherUser.getMobile().equals(this.getMobile())
                && otherUser.getEmail().equals(this.getEmail())) {
            return true;
        }
        return false;


    }

    @Override
    public int hashCode() {
        final int prime =31;
        int result = 1;
        result = prime * result + ((this.firstname==null)? 0 : this.firstname.hashCode());
        result = prime * result + ((this.lastname==null)? 0 : this.lastname.hashCode());
        result = prime * result + ((this.state==null)? 0 : this.state.hashCode());
        result = prime * result + ((this.mobile==null)? 0 : this.mobile.hashCode());
        result = prime * result + ((this.country==null)? 0 : this.country.hashCode());
        result = prime * result + ((this.city==null)? 0 : this.city.hashCode());
        result = prime * result + ((this.profilephoto==null)? 0 : this.profilephoto.hashCode());
        result = prime * result + ((this.address==null)? 0 : this.address.hashCode());
        result = prime * result + ((this.email==null)? 0 : this.email.hashCode());
        return result;

    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);

    }
}
