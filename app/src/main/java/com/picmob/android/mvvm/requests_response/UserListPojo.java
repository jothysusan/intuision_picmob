package com.picmob.android.mvvm.requests_response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class UserListPojo {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    @SerializedName("notifyToken")
    @Expose
    private Object notifyToken;
    @SerializedName("fbToken")
    @Expose
    private Object fbToken;
    @SerializedName("twitterToken")
    @Expose
    private Object twitterToken;
    @SerializedName("avatarURL")
    @Expose
    private Object avatarURL;
    @SerializedName("currentLattitude")
    @Expose
    private String currentLatitude;
    @SerializedName("currentLongitude")
    @Expose
    private String currentLongitude;


    private boolean isChecked;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Object getNotifyToken() {
        return notifyToken;
    }

    public void setNotifyToken(Object notifyToken) {
        this.notifyToken = notifyToken;
    }

    public Object getFbToken() {
        return fbToken;
    }

    public void setFbToken(Object fbToken) {
        this.fbToken = fbToken;
    }

    public Object getTwitterToken() {
        return twitterToken;
    }

    public void setTwitterToken(Object twitterToken) {
        this.twitterToken = twitterToken;
    }

    public Object getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(Object avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(String currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public String getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(String currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public static class Sortbylocation implements Comparator<UserListPojo> {
        // Used for sorting in ascending order of
        // name
        public int compare(UserListPojo a, UserListPojo b)
        {
            return a.location.compareTo(b.location);
        }
    }

}