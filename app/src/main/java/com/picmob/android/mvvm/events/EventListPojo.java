package com.picmob.android.mvvm.events;

public class EventListPojo {
    int id;
    String name;
    String description;
    String displayImageURL;
    boolean isPrivate;
    boolean isReceived;

    public EventListPojo(int id, String name,
                         String description, String displayImageURL,
                         boolean isPrivate, boolean isReceived) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.displayImageURL = displayImageURL;
        this.isPrivate = isPrivate;
        this.isReceived = isReceived;
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

    public String getDisplayImageURL() {
        return displayImageURL;
    }

    public void setDisplayImageURL(String displayImageURL) {
        this.displayImageURL = displayImageURL;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }
}
