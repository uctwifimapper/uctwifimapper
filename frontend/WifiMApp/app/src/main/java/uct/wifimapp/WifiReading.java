package uct.wifimapp;

import com.google.gson.annotations.SerializedName;

/**
 * A single wifi signal reading taken at a specific set of coordinates.
 */
public class WifiReading {

    @SerializedName("bssid")
    private String bssid; //Unique mac address of wireless router

    @SerializedName("signalStrength")
    private int strength; //level of signal strength

    @SerializedName("location")
    private Location location; //location of the router

    @SerializedName("timestamp")
    private long timestamp;

    private double latitude;
    private double longitude;

    public WifiReading() { }

    public WifiReading(String bssid, int strength, Location location, long timestamp) {
        this.bssid = bssid;
        this.strength = strength;
        this.location = location;
        this.timestamp = timestamp;
        this.latitude = this.location.getLatitude();
        this.longitude = this.location.getLongitude();
    }

    public WifiReading(String bssid, double _latitude, double _longitude, int _strength, long timestamp){
        this.bssid = bssid;
        this.latitude = _latitude;
        this.longitude = _longitude;
        this.strength = _strength;
        this.location = new Location(latitude,longitude);
        this.timestamp = timestamp;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public double latitude(){
        return latitude;
    }

    public double longitude(){
        return longitude;
    }

    public int strength(){
        return strength;
    }

    @Override
    public String toString() {
        return "WifiReading{" +
                "bssid='" + bssid + '\'' +
                ", strength=" + strength +
                ", location=" + location.toString() +
                ", timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
