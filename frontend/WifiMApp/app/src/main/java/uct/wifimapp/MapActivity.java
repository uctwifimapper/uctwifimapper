package uct.wifimapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private WifiReadingManager wifiReadingManager;

    /*private LatLngBounds UCT = new LatLngBounds(
            new LatLng(-33.9619445, 18.4592913), new LatLng(-33.9508155, 18.4648683)); //set bounds for map*/
    private int LOCATION_REQUEST_PERMISSION = 1001;
    private WifiMapController wifiMapController;

    private LatLng mapStartLatLng = new LatLng(-33.960848, 18.456848); // Bottom-left corner of map area.
    private double zoneSize = 0.0002000; // Size of grid squares in DD coords.
    private int numZonesX = 32;
    private int numZonesY = 32;

    private LatLngBounds UCT = new LatLngBounds( // Map bounds based on grid.
            mapStartLatLng, new LatLng(mapStartLatLng.latitude + zoneSize * numZonesY, mapStartLatLng.longitude + zoneSize * numZonesX));

    private static final int COLOR_BLACK_ARGB = 0x20ff0000;
    private static final int COLOR_GREY_ARGB = 0x20878787;
    private static final int COLOR_RED_ARGB = 0x20af001a;
    private static final int COLOR_ORANGE_ARGB = 0x20d85d00;
    private static final int COLOR_YELLOW_ARGB = 0x20ffd711;
    private static final int COLOR_GREEN_ARGB = 0x2047b200;
    private static final int COLOR_BLUE_ARGB = 0x2000bca0;
    private static final int POLYGON_STROKE_WIDTH_PX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        wifiReadingManager = new WifiReadingManager(mapStartLatLng.latitude, mapStartLatLng.longitude, zoneSize, numZonesX, numZonesY);
        populateRandomReadings();

        getWifiLocations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
        MenuInflater infl = getMenuInflater();
        infl.inflate(R.menu.settings_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
            default: break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UCT.getCenter(), 18));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_PERMISSION);
        }
        drawMapGrid(wifiReadingManager.getAverageZoneSignalLevels());
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
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                int signalStrength = getCurrentWifiSignalLevel();
                                sendWifiReadingToServer(currentLocation, signalStrength);
                            }
                        }
                    });
        } else { // Show pop-up requesting location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_PERMISSION);
        }
    }

    // Uploads a new wifi signal reading at the user's location to the server.
    private void sendWifiReadingToServer(LatLng location, int signalStrength){
        // TODO
    }

    // Return current wifi signal strength level (0-4).
    private int getCurrentWifiSignalLevel(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int numberOfLevels = 5;
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            return level;
        } else {
            return -1;
        }
    }

    /*
    * Request data from backend using retrofit
    * */
    private void getWifiLocations(){

        Map<String,String> payload = new HashMap<String, String>();
        payload.put("ssid", "eduroam");
        Call<List<AccessPoint>> call = wifiMapController.getInstance().getApn(payload);
        Log.d("MapActivity - payload", payload.toString());
        call.enqueue(new Callback<List<AccessPoint>>(){
            /**
             * Invoked for a received HTTP response.
             * <p>
             * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
             * Call {@link Response#isSuccessful()} to determine if the response indicates success.
             *
             * @param call
             * @param response
             */
            @Override
            public void onResponse(Call<List<AccessPoint>> call, Response<List<AccessPoint>> response) {

                if(response.isSuccessful()){
                    for(AccessPoint accessPoint : response.body()) {
                        //addWifiReading(accessPoint.location.getLatitude(), accessPoint.location.getLongitude(), (int)Math.floor(Math.random()*5));
                    }
                }
            }

            /**
             * Invoked when a network exception occurred talking to the server or when an unexpected
             * exception occurred creating the request or processing the response.
             *
             * @param call
             * @param t
             */
            @Override
            public void onFailure(Call<List<AccessPoint>> call, Throwable t) {

            }
        });
    }

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

    private void stylePolygon(Polygon polygon){
        String type = "";
        if (polygon.getTag() != null){
            type = polygon.getTag().toString();
        }
        int strokeColor = 0;
        int fillColor = COLOR_GREY_ARGB;
        switch (type){
            case "-1": fillColor = 0;
                break;
            case "0": fillColor = COLOR_RED_ARGB;
                break;
            case "1": fillColor = COLOR_ORANGE_ARGB;
                break;
            case "2": fillColor = COLOR_YELLOW_ARGB;
                break;
            case "3": fillColor = COLOR_GREEN_ARGB;
                break;
            case "4": fillColor = COLOR_BLUE_ARGB;
                break;
            default:
                break;
        }
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    private void populateRandomReadings(){
        Random rand = new Random();
        for (int i = 0; i < 300; i++){
            double lat = mapStartLatLng.latitude + rand.nextDouble() * (numZonesY * zoneSize);
            double lng = mapStartLatLng.longitude + rand.nextDouble() * (numZonesX * zoneSize);
            int str = rand.nextInt(5);
            WifiReading reading = new WifiReading(lat, lng, str);
            wifiReadingManager.addWifiReading(reading);
        }
    }
}
