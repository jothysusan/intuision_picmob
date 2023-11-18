package com.picmob.android.mvvm.requests_response;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

public class RequestResponseViewModel extends ViewModel {

    public MutableLiveData<Resource<List<RequestPojo>>> requestLiveData = new MutableLiveData<>();

    public MutableLiveData<Resource<List<RequestPojo>>> responseLiveData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> responseData = new MutableLiveData<>();

    public MutableLiveData<Resource<List<UserListPojo>>> userListData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> requestData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> seenData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> ignoreData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> deleteData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> makePublicData = new MutableLiveData<>();

    public MutableLiveData<Resource<ApiResponseModel>> makePrivateData = new MutableLiveData<>();
    public MutableLiveData<Resource<List<RequestResponseModel>>> dashboardLiveData = new MutableLiveData<>();


    public void initRequest(String token, String id) {
        dashboardLiveData = new RequestResponseRepository()
                .getDashboard(token, id);
    }


    public void getUserListByLocation(String token, LinkedTreeMap<String, Object> map) {

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();

        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        userListData = new RequestResponseRepository().getUserListByLocation(headerMap, map);
    }

    public void sendReponse(String token, LinkedTreeMap<String, Object> linkedTreeMap) {

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();

        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        responseData = new RequestResponseRepository().sendResponse(headerMap, linkedTreeMap);
    }


    public void sendRequest(String token, LinkedTreeMap<String, Object> map) {
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        requestData = new RequestResponseRepository().sendRequest(headerMap, map);
    }

    public void seenResponse(String token, LinkedTreeMap<String, Object> map) {

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        seenData = new RequestResponseRepository().seenResponse(headerMap, map);
    }

    public void ignoreRequest(String token, LinkedTreeMap<String, Object> map) {
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ignoreData = new RequestResponseRepository().ignoreRequest(headerMap, map);
    }

    public void deleteRequest(String token, String type, String requestId) {
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        deleteData = new RequestResponseRepository().deleteRequest(type, requestId, headerMap);
    }

    public void makePublic(String token, LinkedTreeMap<String, Object> map) {
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        makePublicData = new RequestResponseRepository().makePublic(headerMap, map);
    }

    public void makePrivate(String token, LinkedTreeMap<String, Object> map) {
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        makePrivateData = new RequestResponseRepository().makePrivate(headerMap, map);
    }


    public MutableLiveData<Resource<List<RequestPojo>>> getRequestLiveData() {
        return requestLiveData;
    }

    public MutableLiveData<Resource<List<RequestResponseModel>>> getDashboardLiveData() {
        return dashboardLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> sendRespondData() {
        return responseData;
    }


    public MutableLiveData<Resource<List<UserListPojo>>> getUserListData() {
        return userListData;
    }


    public MutableLiveData<Resource<ApiResponseModel>> sendRequestData() {
        return requestData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getSeenData() {
        return seenData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getIgnoreData() {
        return ignoreData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getDeleteData() {
        return deleteData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getMakePublicData() {
        return makePublicData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> getMakePrivateData() {
        return makePrivateData;
    }
}
