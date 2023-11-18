package com.picmob.android.mvvm.events;

import java.io.Serializable;

public class PhonebookUsers implements Serializable {
    String phone_number;

    public PhonebookUsers(String phoneNumber) {
        this.phone_number = phoneNumber;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
