package com.picmob.android.mvvm.friends;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;

import java.util.List;

public class FriendsViewModel extends ViewModel {

    public MutableLiveData<Resource<ApiResponseModel>> inviteData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> requestData = new MutableLiveData<>();

    public MutableLiveData<Resource<List<FriendsPojo>>> friendsListData = new MutableLiveData<>();

    public void sendInvite(String token, LinkedTreeMap<String,Object> list){

        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        inviteData = new FriendsRepository().sendInvite(headerMap,list);
    }


    public void sendFriendRequest(String token, LinkedTreeMap<String,Object> map){

        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        requestData = new FriendsRepository().sendFriendRequest(headerMap,map);
    }


    public void getFriendsList(String token, String id){
        LinkedTreeMap<String ,String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("Authorization","Bearer "+token);

        friendsListData = new FriendsRepository().getFriendsList(headerMap,id);

    }

    public MutableLiveData<Resource<ApiResponseModel>> getInviteData(){
        return inviteData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getRequestData(){ return requestData;}

    public MutableLiveData<Resource<List<FriendsPojo>>> getFriendsListData(){ return friendsListData; }
}
