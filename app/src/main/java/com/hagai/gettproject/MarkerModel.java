package com.hagai.gettproject;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hagai on 8/13/2017.
 */

public class MarkerModel {


    public String name, address;
    public LatLng latLng;

    public MarkerModel(String name, String address, LatLng latLng) {

        this.name = name;
        this.address = address;
        this.latLng = latLng;

    }

}
