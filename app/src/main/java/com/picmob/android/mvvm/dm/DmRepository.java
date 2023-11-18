package com.picmob.android.mvvm.dm;

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

public class DmRepository {

    private static final String TAG = "DmRepository";


    public MutableLiveData<Resource<List<DmPojo>>> getDmMessages(LinkedTreeMap<String, String> headerMap,
                                                                 String usrId, String frndId) {

        final MutableLiveData<Resource<List<DmPojo>>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getDmMessages(headerMap,usrId,frndId).enqueue(new Callback<List<DmPojo>>() {
            @Override
            public void onResponse(@NonNull Call<List<DmPojo>> call, @NonNull Response<List<DmPojo>> response) {
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
            public void onFailure(@NonNull Call<List<DmPojo>> call, @NonNull Throwable t) {
                LogCapture.e(TAG, "onFailure: "+t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
            }
        });

        return mutableLiveData;
    }


    public MutableLiveData<Resource<ApiResponseModel>> sendDM(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {

        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).sendDM(headerMap,map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(new ApiResponseModel("",true)));
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
