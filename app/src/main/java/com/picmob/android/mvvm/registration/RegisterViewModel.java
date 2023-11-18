package com.picmob.android.mvvm.registration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonElement;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.google.gson.internal.LinkedTreeMap;

public class RegisterViewModel extends ViewModel {

    private LiveData<Resource<ApiResponseModel>> resourceLiveData;
    private MutableLiveData<Resource<ApiResponseModel>> validateData;


    public void registerUser(JsonElement linkedTreeMap) {
        resourceLiveData = new RegistrationRepository().registerUser(linkedTreeMap);
    }

    public void validateRegister(LinkedTreeMap<String, Object> map) {
        validateData = new RegistrationRepository().validateRegister(map);
    }

    public LiveData<Resource<ApiResponseModel>> getRegData() {
        return resourceLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getValidateData() {
        return validateData;
    }
}
