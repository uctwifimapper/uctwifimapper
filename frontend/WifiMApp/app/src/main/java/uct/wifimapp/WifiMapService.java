package uct.wifimapp;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/*
    Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati
 *
 *  Retrofit interface showing the api used for connecting to the database.
 * */

public interface WifiMapService {

    @GET("apn")
    Call<List<AccessPoint>> getApn(@QueryMap Map<String, String> query);

    @POST("apn")
    Call<GenericResponse> postApn(@Body ApnPayload apnPayload);

    @GET("strength")
    Call<List<WifiReading>> getSignalStregth(@QueryMap Map<String, String> query);

    @POST("strength")
    Call<GenericResponse> postSignalStrength(@Body WifiReading strengthPayload);

}
