package uct.wifimapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

* Activity responsible for:
* 1. loading the map
* 2. Getting user location
* 3. Getting wifi signal strengths
* 4. Getting accesspoints available for current location/wifi network.
* 5. Display markers on map
* */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private WifiReadingManager wifiReadingManager;

    /*private LatLngBounds UCT = new LatLngBounds(
            new LatLng(-33.9619445, 18.4592913), new LatLng(-33.9508155, 18.4648683)); //set bounds for map*/
    private int LOCATION_REQUEST_PERMISSION = 1001;
    private int WIFI_STATE_REQUEST_PERMISSION = 1002;
    private WifiMapController wifiMapController;
    private WifiInfo wifiInfo;

    private LatLng mapStartLatLng = new LatLng(-33.960848, 18.456848); // Bottom-left corner of map area.
    private double zoneSize = 0.0002000; // Size of grid squares in DD coords.
    private int numZonesX = 32;
    private int numZonesY = 32;

    private LatLngBounds mapBounds = new LatLngBounds( // Map bounds based on grid.
            mapStartLatLng, new LatLng(mapStartLatLng.latitude + zoneSize * numZonesY, mapStartLatLng.longitude + zoneSize * numZonesX));

    private static final boolean USE_SIGNAL_PREDICTION = true;
    private static final int COLOR_RED_ARGB = 0x20ff0000;
    private static final int COLOR_YELLOW_ARGB = 0x20ffff00;
    private static final int COLOR_GREEN_ARGB = 0x2000ff00;
    private static final int COLOR_CYAN_ARGB = 0x2000ffff;
    private static final int COLOR_BLUE_ARGB = 0x200000ff;
    private static final int POLYGON_STROKE_WIDTH_PX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        wifiReadingManager = new WifiReadingManager(mapStartLatLng.latitude, mapStartLatLng.longitude, zoneSize, numZonesX, numZonesY);

        attemptBroadcastWifiReading();
    }

    /* Add options menu to toolbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infl = getMenuInflater();
        infl.inflate(R.menu.settings_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* When option settings is clicked got to settings screen */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.Settings ){
            Intent set = new Intent(this,SettingsActivity.class);
            startActivity(set);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1001:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else {
                    return;
                }
                break;
            case 1002:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else {
                    return;
                }
                break;
            default: break;
        }
    }

    /* Load map and add markers to map */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapBounds.getCenter(), 18));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_PERMISSION);
        }

        if (USE_SIGNAL_PREDICTION) {
            drawMapGrid(wifiReadingManager.getPredictiveAverageZoneSignalLevels());
        } else {
            drawMapGrid(wifiReadingManager.getAverageZoneSignalLevels());
        }
        refreshMap();
    }

    // Attempt to send a wifi reading to the server. If permission is granted, sendWifiReadingToServer is called.
    private void attemptBroadcastWifiReading(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                int signalStrength = getCurrentWifiSignalLevel();
                                sendWifiReadingToServer(new WifiReading(wifiInfo.getBSSID(), location.getLatitude(), location.getLongitude(), signalStrength, System.currentTimeMillis() ));
                            }
                        }
                    });
        } else { // Show pop-up requesting location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_PERMISSION);
        }
    }

    /* Send rsignal strength readings to server*/
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
    // Uploads a new wifi signal reading at the user's location to the server.
    private void sendWifiReadingToServer(LatLng location, int signalStrength){
    }

    // Return current wifi signal strength level (0-4, or -1 if permission not granted).
    private int getCurrentWifiSignalLevel(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();

            int numberOfLevels = 5;
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            return level;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_WIFI_STATE},
                    WIFI_STATE_REQUEST_PERMISSION);
            return -1;
        }
    }

    /*
    * Request data from backend using retrofit TESTED - WORKING
    * */
    private void getWifiLocations(){

        Map<String,String> payload = new HashMap<String, String>();
        payload.put("ssid", "eduroam");
        Call<List<AccessPoint>> call = WifiMapController.getInstance().getApn(payload);
        call.enqueue(new Callback<List<AccessPoint>>(){
            @Override
            public void onResponse(Call<List<AccessPoint>> call, Response<List<AccessPoint>> response) {

                if(response.isSuccessful()){

                }
            }
            @Override
            public void onFailure(Call<List<AccessPoint>> call, Throwable t) {

            }
        });
    }

    // Draw polygons to the map depicting wifi signal zones.
    private void drawMapGrid(int[][] signalLevels){
        for (int x = 0; x < numZonesX; x++) {
            for (int y = 0; y < numZonesY; y++) {
                LatLng bottomLeft = new LatLng(mapStartLatLng.latitude + y * zoneSize, mapStartLatLng.longitude + x * zoneSize);
                Polygon polygon = map.addPolygon(new PolygonOptions()
                        .clickable(false)
                        .add(
                                new LatLng(bottomLeft.latitude, bottomLeft.longitude),
                                new LatLng(bottomLeft.latitude + zoneSize, bottomLeft.longitude),
                                new LatLng(bottomLeft.latitude + zoneSize, bottomLeft.longitude + zoneSize),
                                new LatLng(bottomLeft.latitude, bottomLeft.longitude + zoneSize)));
                polygon.setTag("" + signalLevels[x][y]);
                stylePolygon(polygon);
            }
        }
    }

    // Apply colours etc to map grid squares.
    private void stylePolygon(Polygon polygon){
        String type = "";
        if (polygon.getTag() != null){
            type = polygon.getTag().toString();
        }
        int strokeColor = 0;
        int fillColor = 0;
        switch (type){
            case "-1": fillColor = 0;
                break;
            case "0": fillColor = COLOR_BLUE_ARGB;
                break;
            case "1": fillColor = COLOR_CYAN_ARGB;
                break;
            case "2": fillColor = COLOR_GREEN_ARGB;
                break;
            case "3": fillColor = COLOR_YELLOW_ARGB;
                break;
            case "4": fillColor = COLOR_RED_ARGB;
                break;
            default:
                break;
        }
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    // Fill the map with random readings (for testing).
    private void populateRandomReadings(){

        Random rand = new Random();
        for (int i = 0; i < 10; i++){
            double lat = mapStartLatLng.latitude + rand.nextDouble() * (numZonesY * zoneSize);
            double lng = mapStartLatLng.longitude + rand.nextDouble() * (numZonesX * zoneSize);
            int str = rand.nextInt(5);
            WifiReading reading = new WifiReading("d4:6e:0e:ed:fd:f9",lat, lng, str, 0);
            wifiReadingManager.addWifiReading(reading);
            //sendWifiReadingToServer(reading); //for testing
        }
    }

    // Refresh the map in order to display up-to-date reading data from the server.
    private void refreshMap(){
        map.clear();
        wifiReadingManager = new WifiReadingManager(mapStartLatLng.latitude, mapStartLatLng.longitude, zoneSize, numZonesX, numZonesY);

        Map<String,String> payload = new HashMap<>();
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));
        Call<List<WifiReading>> call = WifiMapController.getInstance().getWifiStrength(payload);
        call.enqueue(new Callback<List<WifiReading>>(){

            @Override
            public void onResponse(Call<List<WifiReading>> call, Response<List<WifiReading>> response) {
                Log.d(this.getClass().getSimpleName(), response.code()+" "+response.body().size());

                if(response.isSuccessful()){

                    for(WifiReading wifiReading : response.body()){
                        wifiReading.setLatitude(wifiReading.getLocation().getLatitude());
                        wifiReading.setLongitude(wifiReading.getLocation().getLongitude());
                        wifiReadingManager.addWifiReading(wifiReading);
                    }

                    if (USE_SIGNAL_PREDICTION) {
                        drawMapGrid(wifiReadingManager.getPredictiveAverageZoneSignalLevels());
                    } else {
                        drawMapGrid(wifiReadingManager.getAverageZoneSignalLevels());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WifiReading>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
