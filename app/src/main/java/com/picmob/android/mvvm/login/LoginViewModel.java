package com.picmob.android.mvvm.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.picmob.android.mvvm.Resource;
import com.google.gson.internal.LinkedTreeMap;

public class LoginViewModel extends ViewModel {

    private LiveData<Resource<UserAuthPojo>> resourceLiveData;
    private LiveData<Resource<UserAuthPojo>> usercheckLiveData;
    private LiveData<Resource<UserAuthPojo>> userRegisterLiveData;


    public void loginUser(LinkedTreeMap<String,Object> linkedTreeMap) {
        resourceLiveData = new LoginRepository().getLoginUser(linkedTreeMap);
    }

    public void loginSocial(LinkedTreeMap<String,Object> linkedTreeMap){
        usercheckLiveData = new LoginRepository().getLoginSocial(linkedTreeMap);
    }


    public void socialRegister(LinkedTreeMap<String,Object> linkedTreeMap){
        userRegisterLiveData = new LoginRepository().registerViaSocial(linkedTreeMap);
    }

    public LiveData<Resource<UserAuthPojo>> getLoginData() {
        return resourceLiveData;
    }

    public LiveData<Resource<UserAuthPojo>> getUsercheckLiveData(){ return usercheckLiveData; }

    public LiveData<Resource<UserAuthPojo>> getUserRegisterLiveData(){ return  userRegisterLiveData;}


}
