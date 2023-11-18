package com.picmob.android.listeners;

import com.picmob.android.mvvm.events.EventImage;
import com.picmob.android.mvvm.gallery.GalleryPojo;

import java.util.List;

public interface EventItemSelectionListener {
    void onCheckboxClick(List<EventImage> pojo);
}