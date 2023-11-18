package com.picmob.android.mvvm.events;

public class UserVicinityPojo {
    int id;
    String username;
    String firstName;
    String lastName;
    String email;
    String location;
    String phoneNumber;
    String notifyToken;
    String fbToken;
    String twitterToken;
    String googleToken;
    String fbId;
    String googleId;
    String twitterId;
    String avatarURL;
    String phoneCode;
    String currentLattitude;
    String currentLongitude;
    boolean isSelected;

    public UserVicinityPojo(int id, String username, String firstName, String lastName, String email,
                            String location, String phoneNumber, String notifyToken, String fbToken,
                            String twitterToken, String googleToken, String fbId, String googleId,
                            String twitterId, String avatarURL, String phoneCode, String currentLattitude,
                            String currentLongitude, boolean isSelected) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.notifyToken = notifyToken;
        this.fbToken = fbToken;
        this.twitterToken = twitterToken;
        this.googleToken = googleToken;
        this.fbId = fbId;
        this.googleId = googleId;
        this.twitterId = twitterId;
        this.avatarURL = avatarURL;
        this.phoneCode = phoneCode;
        this.currentLattitude = currentLattitude;
        this.currentLongitude = currentLongitude;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNotifyToken() {
        return notifyToken;
    }

    public void setNotifyToken(String notifyToken) {
        this.notifyToken = notifyToken;
    }

    public String getFbToken() {
        return fbToken;
    }

    public void setFbToken(String fbToken) {
        this.fbToken = fbToken;
    }

    public String getTwitterToken() {
        return twitterToken;
    }

    public void setTwitterToken(String twitterToken) {
        this.twitterToken = twitterToken;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getCurrentLattitude() {
        return currentLattitude;
    }

    public void setCurrentLattitude(String currentLattitude) {
        this.currentLattitude = currentLattitude;
    }

    public String getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(String currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
