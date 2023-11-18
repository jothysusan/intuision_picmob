package com.picmob.android.mvvm.gallery;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryRepository {
    private static final String TAG = "GalleryRepository";

    public MutableLiveData<Resource<List<GalleryPojo>>> getGalleryList(LinkedTreeMap<String, String> headerMap, String usrId) {

        final MutableLiveData<Resource<List<GalleryPojo>>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getGallery(headerMap, usrId).enqueue(new Callback<List<GalleryPojo>>() {
            @Override
            public void onResponse(@NonNull Call<List<GalleryPojo>> call, @NonNull Response<List<GalleryPojo>> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(response.body()));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong! parseError", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GalleryPojo>> call, @NonNull Throwable t) {
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
                mutableLiveData.setValue(Resource.error("Something went wrong! api failed", null));
            }
        });

        return mutableLiveData;
    }
}
