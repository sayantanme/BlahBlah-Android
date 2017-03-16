package com.example.sayantanchakraborty.blahblah.Model;

import java.io.Serializable;

/**
 * Created by sayantanchakraborty on 16/02/17.
 */

public class User extends Object implements Serializable {
    public String displayName;
    public String email;
    public String profileUrl;
    public String id;

    public User(){

    }

    public User(String displayName, String email, String profileUrl, String id) {
        this.displayName = displayName;
        this.email = email;
        this.profileUrl = profileUrl;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
