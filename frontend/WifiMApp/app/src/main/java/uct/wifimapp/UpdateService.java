package uct.wifimapp;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Class runs in background and sends data to server */
public class UpdateService extends IntentService {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private WifiInfo wifiInfo;
    private Context context;

    public UpdateService(){
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        context = this;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            for (;;){
                SharedPreferences settings = getSharedPreferences("settings.txt", MODE_PRIVATE);
                int sample = settings.getInt("min", 5);
                int min = sample*60*1000;
                attemptBroadcastWifiReading();
                Thread.sleep(min);
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Attempt to send a wifi reading to the server. If permission is granted, sendWifiReadingToServer is called.
    private void attemptBroadcastWifiReading(){

        int signalStrength = getCurrentWifiSignalLevel();
        //sendWifiReadingToServer(new WifiReading(wifiInfo.getBSSID(), 0,0, signalStrength, System.currentTimeMillis() ));

    }

    /* TESTED WORKING */
    private void sendWifiReadingToServer(WifiReading wifiReading){

        Call<GenericResponse> call = WifiMapController.getInstance().postWifiStrength(wifiReading);
        call.enqueue(new Callback<GenericResponse>(){

            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {

            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {

            }
        });
    }

    // Return current wifi signal strength level (0-4, or -1 if permission not granted).
    private int getCurrentWifiSignalLevel(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();

            int numberOfLevels = 5;
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            return level;
        }
        return 0;
    }
}
