package com.picmob.android.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.picmob.android.models.MessageModel;
import com.picmob.android.utils.AppConstants;

import java.util.ArrayList;

public class MessageViewModel extends ViewModel {

    MutableLiveData<ArrayList<MessageModel>> msgLiveData;
    ArrayList<MessageModel> messageModelArrayList;


    public MessageViewModel() {
        msgLiveData = new MutableLiveData<>();
        init();
    }

    public MutableLiveData<ArrayList<MessageModel>> getUserMutableLiveData() {
        return msgLiveData;
    }

    public void init(){
//        populateList();
        messageModelArrayList = new ArrayList<>();
        msgLiveData.setValue(messageModelArrayList);
    }



    public void addValue(MessageModel msgModel){
        messageModelArrayList.add(msgModel);
        msgLiveData.postValue(messageModelArrayList);
    }

    public void populateList(){
        messageModelArrayList = new ArrayList<>();
        messageModelArrayList.add(new MessageModel("Hai", AppConstants.sendMsg));
        messageModelArrayList.add(new MessageModel("Hai", AppConstants.receiveMsg));
        messageModelArrayList.add(new MessageModel("Hello", AppConstants.sendMsg));
        messageModelArrayList.add(new MessageModel("Hello", AppConstants.receiveMsg));
        messageModelArrayList.add(new MessageModel("Initiator here", AppConstants.sendMsg));
        messageModelArrayList.add(new MessageModel("Responser here", AppConstants.receiveMsg));
    }
}
