package uct.wifimapp;

/**
 * A class that holds all wifi reading data for the map,
 * map zone size and locations, and all required helper methods.
 */
public class WifiReadingManager {

    private double zoneSize; // Zone length/width in terms of coordinates
    private double mapStartLat; // Southern-most point
    private double mapStartLng; // Western-most point
    private int numZonesX;
    private int numZonesY;

    private WifiReadingZone[][] wifiReadingZones;

    public WifiReadingManager(double _mapStartLat, double _mapStartLng, double _zoneSize, int _numZonesX, int _numZonesY){
        mapStartLat = _mapStartLat;
        mapStartLng = _mapStartLng;
        zoneSize = _zoneSize;
        numZonesX = _numZonesX;
        numZonesY = _numZonesY;
    }

    // Add a reading to the app's stored map data
    public void addWifiReading(WifiReading reading){
        if (readingOutOfBounds(reading)){
            return;
        }
        int zoneX = (int)Math.floor((reading.longitude() - mapStartLng) / zoneSize);
        int zoneY = (int)Math.floor((reading.latitude() - mapStartLat) / zoneSize);
        wifiReadingZones[zoneX][zoneY].addWifiReading(reading);
    }

    // Upload reading of wifi signal strength at the current location to the server
    public void broadcastWifiReading(){
        // TODO
    }

    private boolean readingOutOfBounds(WifiReading reading){
        if (reading.latitude() < mapStartLat ||
                reading.latitude() > mapStartLat + numZonesY * zoneSize ||
                reading.longitude() < mapStartLng ||
                reading.longitude() > mapStartLng + numZonesX * zoneSize){
            return true;
        }
        return false;
    }

}
