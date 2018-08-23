package uct.wifimapp;

/**
 * A single wifi signal reading taken at a specific set of coordinates.
 */
public class WifiReading {

    private int latitude;
    private int longitude;
    private int strength;

    public WifiReading( int _latitude, int _longitude, int _strength){
        latitude = _latitude;
        longitude = _longitude;
        strength = _strength;
    }

    public int latitude(){
        return latitude;
    }

    public int longitude(){
        return longitude;
    }

    public int strength(){
        return strength;
    }

    public String toString(){
        return latitude + " " + longitude + " " + strength;
    }

}
