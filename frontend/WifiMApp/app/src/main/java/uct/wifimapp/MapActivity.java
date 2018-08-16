package uct.wifimapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        updateUserLocation();
    }

    // Get the most recent user location and centre the camera on it.
    private void updateUserLocation(){
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object.
                                mCurrentLocation = location; // NOTE: This does not seem to persist outside of this method (mCurrentLocation returns null elsewhere)
                                LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                                //mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                            }
                        }
                    });
        } else {
            // Show rationale and request permission.
        }
    }

    // Add a map marker displaying a wifi signal strength reading. (To be replaced by colour coded zones in final app)
    private void addWifiReading(double latitude, double longitude, int strength){
        if (strength < 0 || strength > 4){ // Use enum or something for strength in next version
            return;
        }

        float markerColour;
        switch (strength){
            case 0 : markerColour = BitmapDescriptorFactory.HUE_RED; break;
            case 1 : markerColour = BitmapDescriptorFactory.HUE_ORANGE; break;
            case 2 : markerColour = BitmapDescriptorFactory.HUE_YELLOW; break;
            case 3 : markerColour = BitmapDescriptorFactory.HUE_GREEN; break;
            case 4 : markerColour = BitmapDescriptorFactory.HUE_CYAN; break;
            default : markerColour = BitmapDescriptorFactory.HUE_ROSE; break;
        }

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng( latitude,longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(markerColour)));
    }

    // Return current wifi signal strength level (0-5).
    private int currentWifiSignalLevel(){
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

}
