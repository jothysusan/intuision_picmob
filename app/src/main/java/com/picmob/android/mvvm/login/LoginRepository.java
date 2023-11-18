package com.picmob.android.mvvm.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {

    private static final String TAG = "LoginRepository";

    public LiveData<Resource<UserAuthPojo>> getLoginUser(LinkedTreeMap<String, Object> linkedTreeMap) {
        final MutableLiveData<Resource<UserAuthPojo>> mutableLiveData = new MutableLiveData<>();

        LogCapture.e(TAG, "getLoginUser: " + new Gson().toJson(linkedTreeMap));

        ApiClient.getClient().create(ApiInterface.class).loginUser(linkedTreeMap).enqueue(new Callback<UserAuthPojo>() {
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

    public LiveData<Resource<UserAuthPojo>> getLoginSocial(LinkedTreeMap<String, Object> linkedTreeMap) {
        final MutableLiveData<Resource<UserAuthPojo>> mutableLiveData = new MutableLiveData<>();

        LogCapture.e(TAG, "getLoginUser: " + new Gson().toJson(linkedTreeMap));

        ApiClient.getClient().create(ApiInterface.class).loginSocial(linkedTreeMap).enqueue(new Callback<UserAuthPojo>() {
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

    public LiveData<Resource<UserAuthPojo>> registerViaSocial(LinkedTreeMap<String, Object> linkedTreeMap) {
        final MutableLiveData<Resource<UserAuthPojo>> mutableLiveData = new MutableLiveData<>();

        LogCapture.e(TAG, "getLoginUser: " + new Gson().toJson(linkedTreeMap));

        ApiClient.getClient().create(ApiInterface.class).socialRegister(linkedTreeMap).enqueue(new Callback<UserAuthPojo>() {
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
}
