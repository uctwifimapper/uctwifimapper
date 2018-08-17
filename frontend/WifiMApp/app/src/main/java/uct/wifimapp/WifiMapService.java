package uct.wifimapp;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface WifiMapService {

    @GET("apn")
    Call<List<AccessPoint>> getApn(@QueryMap Map<String, String> query);

    @POST("apn")
    Call<GenericResponse> postApn(@Body ApnPayload apnPayload);
}
