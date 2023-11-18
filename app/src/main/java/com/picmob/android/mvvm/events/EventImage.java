package com.picmob.android.mvvm.events;

public class EventImage {
    int imageId;
    String message;
    String imageUrl;
    String datetime;
    boolean isSelected;
    int userId;
    String userName;

    public EventImage(int imageId, String message, String imageUrl,
                      String datetime, boolean isSelected, int userId, String userName) {
        this.imageId = imageId;
        this.message = message;
        this.imageUrl = imageUrl;
        this.datetime = datetime;
        this.isSelected = isSelected;
        this.userId = userId;
        this.userName = userName;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
