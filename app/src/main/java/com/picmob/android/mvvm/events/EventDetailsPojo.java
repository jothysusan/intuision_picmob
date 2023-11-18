package com.picmob.android.mvvm.events;

import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;

import java.util.ArrayList;

public class EventDetailsPojo {
    int id;
    String name;
    String description;
    String type;
    int hostId;
    String hostName;
    ArrayList<EventImage> images;
    ArrayList<EventUsers> users;
    ArrayList<EventUsers> nearyby_users;
    ArrayList<EventUsers> registered_users;
    ArrayList<PhonebookUsers> phonebook_users;
    String latitude;
    String longitude;

    public EventDetailsPojo(int id, String name, String description, String type, int hostId,
                            String hostName, ArrayList<EventImage> images, ArrayList<EventUsers> users,
                            ArrayList<EventUsers> nearyby_users,
                            ArrayList<EventUsers> registered_users, ArrayList<PhonebookUsers> phonebook_users,
                            String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.hostId = hostId;
        this.hostName = hostName;
        this.images = images;
        this.users = users;
        this.nearyby_users = nearyby_users;
        this.registered_users = registered_users;
        this.phonebook_users = phonebook_users;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public ArrayList<EventImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<EventImage> images) {
        this.images = images;
    }

    public ArrayList<EventUsers> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<EventUsers> users) {
        this.users = users;
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
}
