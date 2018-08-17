package uct.wifimapp;

import java.util.Dictionary;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WifiMapService {

    @GET("apn")
    Call<List<AccessPoint>> getApn(@FieldMap Dictionary<?,?> query);

    @POST("apn")
    Call<GenericResponse> postApn(@Body ApnPayload apnPayload);
}
