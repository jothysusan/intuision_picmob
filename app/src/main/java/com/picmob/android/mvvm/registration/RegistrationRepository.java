package com.picmob.android.mvvm.registration;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonElement;
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

public class RegistrationRepository {
    private static final String TAG = "RegistrationRepository";

    public LiveData<Resource<ApiResponseModel>> registerUser(JsonElement linkedTreeMap){
        LogCapture.e(TAG, "registerUser: "+new Gson().toJson(linkedTreeMap) );
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).registerUser(linkedTreeMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Registered successfully",true)));
                }else {
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
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return  mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> validateRegister(LinkedTreeMap<String, Object> map) {
        LogCapture.e(TAG, "validateRegister: "+new Gson().toJson(map));

        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).validateRegister(map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("All information are valid",true)));
                }else {
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
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }
}
