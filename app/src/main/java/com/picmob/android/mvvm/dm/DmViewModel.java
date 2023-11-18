package com.picmob.android.mvvm.dm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;

import java.util.List;

public class DmViewModel extends ViewModel {

    public MutableLiveData<Resource<List<DmPojo>>> dmSData = new MutableLiveData<>();
    public MutableLiveData<Resource<List<DmPojo>>> dmRData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> dmMsgData = new MutableLiveData<>();

    public void getMessages(String token, String usrId, String frndId) {

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        dmSData = new DmRepository().getDmMessages(headerMap, usrId, frndId);
        dmRData = new DmRepository().getDmMessages(headerMap, frndId, usrId);
    }

    public void sendDM(String token, LinkedTreeMap<String, Object> map) {

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        dmMsgData = new DmRepository().sendDM(headerMap, map);
    }

    public MutableLiveData<Resource<List<DmPojo>>> getDmSData() {
        return dmSData;
    }

    public MutableLiveData<Resource<List<DmPojo>>> getDmRData() {
        return dmRData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getDmMsgData() {
        return dmMsgData;
    }
}
