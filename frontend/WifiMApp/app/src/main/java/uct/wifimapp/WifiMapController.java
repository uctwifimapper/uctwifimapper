package uct.wifimapp;

import java.util.Dictionary;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WifiMapController {

    Retrofit retrofit = null;

    private static WifiMapController instance;
    private static WifiMapService wifiMapService;

    public static WifiMapController getInstance() {

        if (null == instance) {
            instance = new WifiMapController();
        }

        if (null == wifiMapService) {
            String baseURL = "";

            instance.retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            wifiMapService = instance.retrofit.create(WifiMapService.class);
        }

        return instance;
    }

    public Call<List<AccessPoint>> getApn(Dictionary<?,?> query) {
        Call<List<AccessPoint>> call = wifiMapService.getApn(query);
        return call;
    }

    public Call<GenericResponse> postApn(ApnPayload payload) {
        Call<GenericResponse> call = wifiMapService.postApn(payload);
        return call;
    }
}
