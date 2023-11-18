package com.picmob.android.listeners;

import com.picmob.android.mvvm.events.EventListPojo;
import com.picmob.android.mvvm.events.EventsPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;

public interface EventClickListeners {
    void onClickEventItem(EventListPojo eventsPojo);
}
