package com.picmob.android.listeners;

import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.RequestResponsePoJo;

public interface RequestResponseClickListener {
    void onClickReqItem(RequestResponseModel responseModel);
}
