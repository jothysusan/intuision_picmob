package com.picmob.android.mvvm.requests_response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestPojo {

    @SerializedName("userId")
    @Expose
    private Integer userId;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("messageId")
    @Expose
    private Integer messageId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("mediaURL")
    @Expose
    private String mediaURL;
    @SerializedName("dateTime")
    @Expose
    private String dateTime;
    @SerializedName("makePublic")
    @Expose
    private Integer makePublic;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
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

    public Integer getMakePublic() {
        return makePublic;
    }

    public void setMakePublic(Integer makePublic) {
        this.makePublic = makePublic;
    }
}
