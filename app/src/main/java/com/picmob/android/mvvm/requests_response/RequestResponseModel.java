package com.picmob.android.mvvm.requests_response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestResponseModel {

    private Integer userId;
    private String userName;
    private boolean request;
    @SerializedName("messageId")
    @Expose
    private Integer messageId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("mediaURL")
    @Expose
    private String mediaURL;
    @SerializedName("dateTime")
    @Expose
    private String dateTime;
    @SerializedName("makePublic")
    @Expose
    private Integer makePublic;
    @SerializedName("users")
    @Expose
    private List<UserPojo> users;

    public RequestResponseModel(Integer userId, String userName, boolean request, Integer messageId,
                                String message, Integer type, String mediaURL, String dateTime,
                                Integer makePublic, List<UserPojo> users) {
        this.userId = userId;
        this.userName = userName;
        this.request = request;
        this.messageId = messageId;
        this.message = message;
        this.type = type;
        this.mediaURL = mediaURL;
        this.dateTime = dateTime;
        this.makePublic = makePublic;
        this.users = users;
    }

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

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public List<UserPojo> getUsers() {
        return users;
    }

    public void setUsers(List<UserPojo> users) {
        this.users = users;
    }
}
