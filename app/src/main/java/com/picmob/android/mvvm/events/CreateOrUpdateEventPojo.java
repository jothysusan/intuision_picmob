package com.picmob.android.mvvm.events;

import java.util.ArrayList;

public class CreateOrUpdateEventPojo {
    int id;
    int user_id;
    String name;
    String description;
    String display_image_url;
    String type;
    String latitude;
    String longitude;
    ArrayList<EventUsers> nearyby_users;
    ArrayList<EventUsers> registered_users;
    ArrayList<PhonebookUsers> phonebook_users;

    public CreateOrUpdateEventPojo(int id, int user_id, String name, String description, String display_image_url,
                                   String type, String latitude, String longitude,
                                   ArrayList<EventUsers> nearyby_users, ArrayList<EventUsers> registered_users,
                                   ArrayList<PhonebookUsers> phonebook_users) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.description = description;
        this.display_image_url = display_image_url;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nearyby_users = nearyby_users;
        this.registered_users = registered_users;
        this.phonebook_users = phonebook_users;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_image_url() {
        return display_image_url;
    }

    public void setDisplay_image_url(String display_image_url) {
        this.display_image_url = display_image_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public ArrayList<EventUsers> getNearyby_users() {
        return nearyby_users;
    }

    public void setNearyby_users(ArrayList<EventUsers> nearyby_users) {
        this.nearyby_users = nearyby_users;
    }

    public ArrayList<EventUsers> getRegistered_users() {
        return registered_users;
    }

    public void setRegistered_users(ArrayList<EventUsers> registered_users) {
        this.registered_users = registered_users;
    }

    public ArrayList<PhonebookUsers> getPhonebook_users() {
        return phonebook_users;
    }

    public void setPhonebook_users(ArrayList<PhonebookUsers> phonebook_users) {
        this.phonebook_users = phonebook_users;
    }
}



