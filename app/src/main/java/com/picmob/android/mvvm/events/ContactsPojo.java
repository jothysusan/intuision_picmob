package com.picmob.android.mvvm.events;

import android.net.Uri;

import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;

import java.util.ArrayList;
import java.util.List;

public class ContactsPojo {
    private String mDisplayName;
//    private List<PhoneNumber> mPhoneNumbers = new ArrayList<>();
    String phoneNumber;

    public ContactsPojo(String mDisplayName, String mPhoneNumber) {
        this.mDisplayName = mDisplayName;
        this.phoneNumber = mPhoneNumber;
    }

    public String getmDisplayName() {
        return mDisplayName;
    }

    public void setmDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
