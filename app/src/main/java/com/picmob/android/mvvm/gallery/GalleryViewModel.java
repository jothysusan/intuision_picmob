package com.picmob.android.mvvm.gallery;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.picmob.android.mvvm.Resource;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

public class GalleryViewModel extends ViewModel {

    MutableLiveData<Resource<List<GalleryPojo>>> galleryData = new MutableLiveData<>();

    public void getGalleryList(String token,String usrId){

        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        galleryData = new GalleryRepository().getGalleryList(headerMap,usrId);
    }


    public MutableLiveData<Resource<List<GalleryPojo>>> getGalleryData(){
        return galleryData;
    }

}
