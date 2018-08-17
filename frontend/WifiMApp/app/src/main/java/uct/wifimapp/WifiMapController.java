package uct.wifimapp;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
    Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

    Setups Retrofit singleton object that will be used for communicating with server
    Contains business logic TODO implement more methods
    */
public class WifiMapController {

    Retrofit retrofit = null;

    private static WifiMapController instance;
    private static WifiMapService wifiMapService;

    public static WifiMapController getInstance() {

        if (null == instance) {
            instance = new WifiMapController();
        }

        if (null == wifiMapService) {
            String baseURL = "http://10.0.2.2:8800/"; //localhost

            instance.retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            wifiMapService = instance.retrofit.create(WifiMapService.class);
        }

        return instance;
    }

    public Call<List<AccessPoint>> getApn(Map<String,String> query) {
        Call<List<AccessPoint>> call = wifiMapService.getApn(query);
        return call;
    }

    public Call<GenericResponse> postApn(ApnPayload payload) {
        Call<GenericResponse> call = wifiMapService.postApn(payload);
        return call;
    }
}
