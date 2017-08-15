package com.hagai.gettproject.comm;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by hagai on 8/12/2017.
 */

public interface ApiInterface {

    @GET("place/nearbysearch/json?")
    Call<PlacesResult.Root> findPlaces(@Query(value = "location", encoded = true) String location, @Query(value = "radius", encoded = true) int radius, @Query(value = "key", encoded = true) String key);

    @GET("geocode/json?")
    Call<ReverseResult.Root> reverseGeocoding(@Query(value = "latlng", encoded = true) String latlng, @Query(value = "key", encoded = true) String key);

}
