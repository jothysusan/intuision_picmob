package com.picmob.android.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MapClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;

    public MapClusterItem(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}