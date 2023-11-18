package com.picmob.android.mvvm.forget_password;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.login.UserAuthPojo;

public class ForgetPasswordViewModel extends ViewModel {
    public MutableLiveData<Resource<UserAuthPojo>> userLiveData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> resetPswrdData = new MutableLiveData<>();


    public void getUserByPhone(String phone) {
        userLiveData = new ForgetPasswordRepository().getUserByPhone(phone);
    }

    public void resetPassword(String id,String token, LinkedTreeMap<String,String> map){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);
        resetPswrdData = new ForgetPasswordRepository().resetPassword(id,headerMap,map);
    }

    public MutableLiveData<Resource<UserAuthPojo>> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getResetPswrdData(){ return resetPswrdData;}
}
