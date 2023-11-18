package com.picmob.android.mvvm.events;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.utils.AppConstant;

import java.util.List;

public class EventViewModel extends ViewModel {
    public MutableLiveData<Resource<List<EventListPojo>>> eventLiveData = new MutableLiveData<>();
    public MutableLiveData<Resource<List<UserVicinityPojo>>> userVicinityLiveData = new MutableLiveData<>();
    public MutableLiveData<Resource<List<UserVicinityPojo>>> registeredUserLiveData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> createEventData = new MutableLiveData<>();
    public MutableLiveData<Resource<EventDetailsPojo>> fetchEventData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> deleteEventData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> deleteEventImageData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> attachEventImageData = new MutableLiveData<>();
    public MutableLiveData<Resource<ApiResponseModel>> updateEventData = new MutableLiveData<>();

    public void getUsersList(String token, LinkedTreeMap<String, Object> map) {
        userVicinityLiveData = new EventsRepository().getUserInVicinity(token, map);
        map.remove(AppConstant.LATITUDE);
        map.remove(AppConstant.LONGITUDE);
        map.remove(AppConstant.RADIUS);
        registeredUserLiveData = new EventsRepository().getRegisteredUsers(token, map);
    }

    public MutableLiveData<Resource<List<UserVicinityPojo>>> getUserVicinityLiveData() {
        return userVicinityLiveData;
    }

    public MutableLiveData<Resource<List<UserVicinityPojo>>> getRegisteredUserLiveData() {
        return registeredUserLiveData;
    }

    public void sendEvent(String token, CreateOrUpdateEventPojo createOrUpdateEventPojo, boolean isUpdate) {
        createEventData = new EventsRepository().sendEvent(token, createOrUpdateEventPojo, isUpdate);
    }

    public MutableLiveData<Resource<ApiResponseModel>> createEventData() {
        return createEventData;
    }

    public void getEventList(String token, LinkedTreeMap<String, Object> map) {
        eventLiveData = new EventsRepository().getEvenList(token, map);
    }

    public MutableLiveData<Resource<List<EventListPojo>>> getEventListLiveData() {
        return eventLiveData;
    }

    public void fetchEventDetails(String token, LinkedTreeMap<String, Object> map) {
        fetchEventData = new EventsRepository().fetchEvent(token, map);
    }

    public MutableLiveData<Resource<EventDetailsPojo>> fetchEventDetailsLiveData() {
        return fetchEventData;
    }

    public void deleteEvent(String token, int eventID) {
        deleteEventData = new EventsRepository().deleteEvent(token, eventID);
    }

    public MutableLiveData<Resource<ApiResponseModel>> deleteEventData() {
        return deleteEventData;
    }

    public void deleteEventImage(String token, LinkedTreeMap<String, Object> map) {
        deleteEventImageData = new EventsRepository().deleteEventImage(token, map);
    }

    public MutableLiveData<Resource<ApiResponseModel>> deleteEventImageData() {
        return deleteEventImageData;
    }

    public void attachEventImage(String token, LinkedTreeMap<String, Object> map) {
        attachEventImageData = new EventsRepository().attachImageToEvent(token, map);
    }

    public MutableLiveData<Resource<ApiResponseModel>> attachEventImageData() {
        return attachEventImageData;
    }
}
