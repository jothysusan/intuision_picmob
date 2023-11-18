package com.picmob.android.mvvm.profile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.google.gson.internal.LinkedTreeMap;

public class ProfileViewModel extends ViewModel {

    MutableLiveData<Resource<ProfilePojo>> profileData = new MutableLiveData<>();

    MutableLiveData<Resource<ApiResponseModel>> locationData = new MutableLiveData<>();

    MutableLiveData<Resource<ApiResponseModel>> imageData = new MutableLiveData<>();

    MutableLiveData<Resource<ApiResponseModel>> updateProfileData = new MutableLiveData<>();

    public void getUserDetails(String token,String usrId){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        profileData = new ProfileRepository().getProfile(headerMap,usrId);
    }

    public void updateUserData(String token,String usrId,LinkedTreeMap<String,Object> map){

        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        updateProfileData = new ProfileRepository().updateUserData(headerMap,usrId,map);
    }


    public void updateLocation(String token, String usrId,LinkedTreeMap<String,String> location){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        locationData = new ProfileRepository().updateLocation(headerMap,usrId,location);
    }

    public void updateImage(String token, String usrId,LinkedTreeMap<String,String> image){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        imageData = new ProfileRepository().updateProfileImage(headerMap,usrId,image);
    }


    public MutableLiveData<Resource<ProfilePojo>> getProfileData(){ return profileData; }


    public MutableLiveData<Resource<ApiResponseModel>> getLocationData(){return locationData;}


    public MutableLiveData<Resource<ApiResponseModel>> getImageData(){return imageData;}

    public MutableLiveData<Resource<ApiResponseModel>> getUpdateProfileData(){return  updateProfileData;}
}
