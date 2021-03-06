package uct.wifimapp;

import com.google.gson.annotations.SerializedName;

/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati
 *
 * Class represents location coordinates(Latitude, Longitude)
 * */
public class Location {
    @SerializedName("x")
    private double latitude;
    @SerializedName("y")
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
