package com.picmob.android.mvvm.fcm;

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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmRepository {

    private static final String TAG = "FcmRepository";

    public MutableLiveData<Resource<ApiResponseModel>> updateToken(LinkedTreeMap<String, String> headerMap,
                                                                   String usrId, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).updateNotificationToken(headerMap,usrId,map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("Fcm token updated",true)));
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
