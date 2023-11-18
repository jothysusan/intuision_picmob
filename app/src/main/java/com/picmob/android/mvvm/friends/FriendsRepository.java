package com.picmob.android.mvvm.friends;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsRepository {

    private static final String TAG = "FriendsRepository";

    public MutableLiveData<Resource<ApiResponseModel>> sendInvite(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> list) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        Log.e(TAG, "sendInvite: "+new Gson().toJson(list) );

        ApiClient.getClient().create(ApiInterface.class).inviteContact(headerMap,list).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Invite sent",true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                LogCapture.e(TAG, "onFailure: "+t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
            }
        });

        return mutableLiveData;
    }


    public MutableLiveData<Resource<ApiResponseModel>> sendFriendRequest(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        Log.e(TAG, "sendInvite: "+new Gson().toJson(map) );

        ApiClient.getClient().create(ApiInterface.class).addFriend(headerMap,map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("User added to your friend list",true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                LogCapture.e(TAG, "onFailure: "+t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<FriendsPojo>>> getFriendsList(LinkedTreeMap<String, String> headerMap, String id) {
        final MutableLiveData<Resource<List<FriendsPojo>>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getFriends(headerMap,id).enqueue(new Callback<List<FriendsPojo>>() {
            @Override
            public void onResponse(Call<List<FriendsPojo>> call, Response<List<FriendsPojo>> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(response.body()));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<List<FriendsPojo>> call, Throwable t) {
                LogCapture.e(TAG, "onFailure: "+t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
            }
        });

        return mutableLiveData;
    }
}
