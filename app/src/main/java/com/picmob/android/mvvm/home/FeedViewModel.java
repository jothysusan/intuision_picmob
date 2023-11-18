package com.picmob.android.mvvm.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.gallery.GalleryRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    public MutableLiveData<Resource<List<GalleryPojo>>> feedData = new MutableLiveData<>();

    public void getFeedList(String token,String usrId){

        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        feedData = new HomeRepository().getFeedList(headerMap,usrId);
    }

    public void addFeed(GalleryPojo pojo){
        List<GalleryPojo> pojoList = new ArrayList<>();
//        pojoList.addAll(feedData.getValue().data);
        pojoList.add(pojo);
        feedData.postValue(Resource.success(pojoList));
    }

    public MutableLiveData<Resource<List<GalleryPojo>>> getFeedData() {
        return feedData;
    }
}
