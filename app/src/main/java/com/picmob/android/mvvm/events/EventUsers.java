package com.picmob.android.mvvm.events;

import java.io.Serializable;

public class EventUsers implements Serializable {
    int userid;
    String username;

    public EventUsers(int userid) {
        this.userid = userid;
    }

    public EventUsers(int userid, String username) {
        this.userid = userid;
        this.username = username;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
