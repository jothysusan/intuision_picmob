package com.picmob.android.mvvm.forget_password;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordRepository {
    private static final String TAG = "ForgetPasswordRepositor";

    public MutableLiveData<Resource<UserAuthPojo>> getUserByPhone(String phone) {
        LogCapture.e(TAG, "getUserByPhone: "+phone );
        final MutableLiveData<Resource<UserAuthPojo>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getUserByPhone(phone).enqueue(new Callback<UserAuthPojo>() {
            @Override
            public void onResponse(@NonNull Call<UserAuthPojo> call, @NonNull Response<UserAuthPojo> response) {
                if (response.isSuccessful()) {
                    UserAuthPojo body = response.body();
                    mutableLiveData.setValue(Resource.success(body));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), response.body()));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", response.body()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserAuthPojo> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }


    public MutableLiveData<Resource<ApiResponseModel>> resetPassword(String id,
                                                                     LinkedTreeMap<String, String> headerMap,
                                                                     LinkedTreeMap<String, String> map) {

        LogCapture.e(TAG, "resetPassword: "+new Gson().toJson(map)+ "\n"+id+"\n"+new Gson().toJson(headerMap));
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).resetPassword(headerMap,id,map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("password resetted",true)));
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
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }
}
