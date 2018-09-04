package uct.wifimapp;

/**
 * A single wifi signal reading taken at a specific set of coordinates.
 */
public class WifiReading {

    private double latitude;
    private double longitude;
    private int strength;

    public WifiReading( double _latitude, double _longitude, int _strength){
        latitude = _latitude;
        longitude = _longitude;
        strength = _strength;
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

    public String toString(){
        return latitude + " " + longitude + " " + strength;
    }

}
