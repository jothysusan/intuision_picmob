package com.picmob.android.mvvm.webservices;

import com.google.gson.JsonElement;
import com.picmob.android.mvvm.dm.DmPojo;
import com.picmob.android.mvvm.events.CreateOrUpdateEventPojo;
import com.picmob.android.mvvm.events.EventDetailsPojo;
import com.picmob.android.mvvm.events.EventListPojo;
import com.picmob.android.mvvm.events.UserVicinityPojo;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.profile.ProfilePojo;
import com.picmob.android.mvvm.requests_response.RequestPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.UserListPojo;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("/users/register")
    Call<ResponseBody> registerUser(@Body JsonElement jsonElement);

    @Headers("Content-Type: application/json")
    @POST("users/validateRegister")
    Call<ResponseBody> validateRegister(@Body Map<String, Object> map);

    @Headers("Content-Type: application/json")
    @POST("/users/authenticate")
    Call<UserAuthPojo> loginUser(@Body Map<String, Object> map);

    @Headers("Content-Type: application/json")
    @POST("/users/authenticateSocial")
    Call<UserAuthPojo> loginSocial(@Body Map<String, Object> map);

    @Headers("Content-Type: application/json")
    @POST("/users/SocialRegister")
    Call<UserAuthPojo> socialRegister(@Body Map<String, Object> map);

    @Headers("Content-Type: application/json")
    @GET("/users/getUserByPhone/{phone}")
    Call<UserAuthPojo> getUserByPhone(@Path("phone") String phone);


    /*@GET("geoMessages/getRequests/{usrId}")
    Call<List<RequestPojo>> getRequests(@Header("Authorization") String token, @Path("usrId") String id);*/

    @GET("geoMessages/getRequests/{usrId}")
    Call<List<RequestPojo>> getRequests(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @GET("geoMessages/getResponses/{usrId}")
    Call<List<RequestPojo>> getResponses(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @POST("geoMessages/sendResponse")
    Call<ResponseBody> sendResponse(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("geoMessages/sendRequest")
    Call<ResponseBody> sendRequest(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("users/getProximityByCurrentLocation")
    Call<List<UserListPojo>> getUserListByLocation(@HeaderMap Map<String, String> map,
                                                   @Body Map<String, Object> location);

    @GET("users/{usrId}")
    Call<ProfilePojo> getUserProfile(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @PUT("users/{usrId}/updateLocation")
    Call<ResponseBody> updateLocation(@HeaderMap Map<String, String> map, @Path("usrId") String id, @Body Map<String, String> location);

    @PUT("users/{usrId}/updateAvatarURL")
    Call<ResponseBody> updateProfileImage(@HeaderMap Map<String, String> map, @Path("usrId") String id, @Body Map<String, String> image);

    @PUT("users/{usrId}/updateNotifyToken")
    Call<ResponseBody> updateNotificationToken(@HeaderMap Map<String, String> map, @Path("usrId") String id, @Body Map<String, Object> maps);

    @PUT("/users/{id}/updatePassword")
    Call<ResponseBody> resetPassword(@HeaderMap Map<String, String> map, @Path("id") String id, @Body Map<String, String> maps);

    @PUT("users/{usrId}/updateUser")
    Call<ResponseBody> updateUser(@HeaderMap Map<String, String> map, @Path("usrId") String id, @Body Map<String, Object> maps);

    @GET("geoMessages/getGalleryItems/{usrId}")
    Call<List<GalleryPojo>> getGallery(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @GET("geoMessages/getLocationFeed/{usrId}")
    Call<List<GalleryPojo>> getFeed(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @POST("geoMessages/seenResponse")
    Call<ResponseBody> seenResponse(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("geoMessages/ignoreRequest")
    Call<ResponseBody> ignoreRequest(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @DELETE("geoMessages/deleteRecordsByMessageId/{type}/{messageId}")
    Call<ResponseBody> deleteRequest(@Path("type") String type, @Path("messageId") String messageId,
                                     @HeaderMap Map<String, String> map);

    @POST("geoMessages/makePublic")
    Call<ResponseBody> makePublic(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("geoMessages/makePrivate")
    Call<ResponseBody> makePrivate(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("/friends/invitePhoneContacts")
    Call<ResponseBody> inviteContact(@HeaderMap Map<String, String> map, @Body Map<String, Object> list);

    @POST("/friends/addFriend")
    Call<ResponseBody> addFriend(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @GET("/friends/{id}")
    Call<List<FriendsPojo>> getFriends(@HeaderMap Map<String, String> map, @Path("id") String id);

    @GET("/dmdetails/user/{usrId}/friend/{frndId}")
    Call<List<DmPojo>> getDmMessages(@HeaderMap Map<String, String> map, @Path("usrId") String usrId, @Path("frndId") String frndId);

    @POST("/dmdetails/sendDM")
    Call<ResponseBody> sendDM(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @PUT("users/updateCurrentLocation")
    Call<ResponseBody> updateCurrentLocation(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @GET("geoMessages/getDashboardDetails/{usrId}")
    Call<List<RequestResponseModel>> getDashboard(@HeaderMap Map<String, String> map, @Path("usrId") String id);

    @POST("/events/getNearByUsers")
    Call<List<UserVicinityPojo>> getUserVicinity(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("/events/getRegisteredUsers")
    Call<List<UserVicinityPojo>> getRegisteredUsers(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("/events/create")
    Call<ResponseBody> sendEvent(@HeaderMap Map<String, String> map, @Body CreateOrUpdateEventPojo createOrUpdateEventPojo);

    @POST("events/getEvents")
    Call<List<EventListPojo>> getEventList(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("/events/details")
    Call<EventDetailsPojo> fetchEventDetail(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @DELETE("/events/delete/{id}")
    Call<ResponseBody> deleteEvent(@HeaderMap Map<String, String> map, @Path("id") int eventId);

    @POST("/events/deleteImage")
    Call<ResponseBody> deleteEventImage(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);

    @POST("/events/attachImage")
    Call<ResponseBody> attachImageToEvent(@HeaderMap Map<String, String> map, @Body Map<String, Object> maps);
}
