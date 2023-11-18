package com.picmob.android.mvvm.dm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DmPojo {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("senderId")
    @Expose
    private Integer senderId;
    @SerializedName("receiverId")
    @Expose
    private Integer receiverId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("mediaURL")
    @Expose
    private String mediaURL;
    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

}
