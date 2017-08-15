package com.hagai.gettproject.comm;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hagai on 8/12/2017.
 */
public class PlacesResult {

    public class Root implements Serializable {
        @SerializedName("results")
        public List<Results> customA = new ArrayList<>();
        @SerializedName("status")
        public String status;
    }

    public class Results implements Serializable {
        @SerializedName("geometry")
        public Geometry geometry;
        @SerializedName("vicinity")
        public String vicinity;
        @SerializedName("name")
        public String name;
    }

    public class Geometry implements Serializable {
        @SerializedName("location")
        public LocationA locationA;
    }

    public class LocationA implements Serializable {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }


}
