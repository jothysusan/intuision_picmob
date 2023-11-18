package com.picmob.android.mvvm.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";

    public MutableLiveData<Resource<ProfilePojo>> getProfile(LinkedTreeMap<String ,String> token, String usrId) {
        final MutableLiveData<Resource<ProfilePojo>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getUserProfile(token,usrId).enqueue(new Callback<ProfilePojo>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePojo> call, @NonNull Response<ProfilePojo> response) {
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
            public void onFailure(@NonNull Call<ProfilePojo> call, @NonNull Throwable t) {
                LogCapture.e(TAG, "onFailure: "+t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> updateLocation(LinkedTreeMap<String, String> headerMap, String usrId,
                                                                      LinkedTreeMap<String,String> location) {

        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).updateLocation(headerMap,usrId,location).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Location updated successfully",true)));
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

    public MutableLiveData<Resource<ApiResponseModel>> updateProfileImage(LinkedTreeMap<String, String> headerMap, String usrId, LinkedTreeMap<String, String> image) {

        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).updateProfileImage(headerMap,usrId,image).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Profile picture updated successfully",true)));
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

    public MutableLiveData<Resource<ApiResponseModel>> updateUserData(LinkedTreeMap<String, String> headerMap, String usrId,LinkedTreeMap<String, Object> map) {

        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).updateUser(headerMap,usrId,map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Profile updated successfully",true)));
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
}


