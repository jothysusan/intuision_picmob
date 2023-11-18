package com.picmob.android.mvvm.fcm;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;

public class FcmViewModel extends ViewModel {
    private static final String TAG = "FcmViewModel";

    public MutableLiveData<Resource<ApiResponseModel>> fcmData = new MutableLiveData<>();

    public void updateFcmToken(String token, String usrId, LinkedTreeMap<String,Object> map){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        Log.e(TAG, "updateFcmToken: "+token+"\n userId=>"+usrId );

        fcmData = new FcmRepository().updateToken(headerMap,usrId,map);
    }

    public MutableLiveData<Resource<ApiResponseModel>> getFcmData(){
        return fcmData;
    }
}
