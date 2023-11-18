package com.picmob.android.mvvm.events;

import com.picmob.android.mvvm.gallery.GalleryPojo;

import java.util.List;

public class EventsPojo {
    String name;
    String description;
    String displayImageURL;
    boolean isPrivate;
    int id;
    int type;
    String hostedBy;
    List<GalleryPojo> eventImages;
    boolean isReceived;

    public EventsPojo(String name, String description, String displayImageURL,
                      boolean isPrivate, int id, int type, String hostedBy,
                      List<GalleryPojo> eventImages, boolean isReceived) {
        this.name = name;
        this.description = description;
        this.displayImageURL = displayImageURL;
        this.isPrivate = isPrivate;
        this.id = id;
        this.type = type;
        this.hostedBy = hostedBy;
        this.eventImages = eventImages;
        this.isReceived = isReceived;
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

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHostedBy() {
        return hostedBy;
    }

    public void setHostedBy(String hostedBy) {
        this.hostedBy = hostedBy;
    }

    public List<GalleryPojo> getEventImages() {
        return eventImages;
    }

    public void setEventImages(List<GalleryPojo> eventImages) {
        this.eventImages = eventImages;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }
}
