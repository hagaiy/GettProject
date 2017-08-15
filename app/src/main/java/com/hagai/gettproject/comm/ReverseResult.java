package com.hagai.gettproject.comm;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hagai on 8/13/2017.
 */

public class ReverseResult {

    public class Root implements Serializable {

        @SerializedName("results")
        public List<Results> customA = new ArrayList<>();
        @SerializedName("status")
        public String status;

    }

    public class Results implements Serializable {

        //        @SerializedName("address_components")
        @SerializedName("formatted_address")
        public String address;
//        @SerializedName("place_id")
//        @SerializedName("types")
//        @SerializedName("geometry")
//        public ReverseResult.Geometry geometry;

    }


}
