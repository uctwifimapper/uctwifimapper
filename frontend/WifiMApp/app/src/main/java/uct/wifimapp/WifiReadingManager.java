package uct.wifimapp;

import android.util.Log;

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
        wifiReadingZones = new WifiReadingZone[numZonesX][numZonesY];
        for (int x = 0; x < numZonesX; x++){
            for (int y = 0; y < numZonesY; y++){
                wifiReadingZones[x][y] = new WifiReadingZone();
            }
        }
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

    private boolean readingOutOfBounds(WifiReading reading){
        if (reading.latitude() < mapStartLat ||
                reading.latitude() > mapStartLat + numZonesY * zoneSize ||
                reading.longitude() < mapStartLng ||
                reading.longitude() > mapStartLng + numZonesX * zoneSize){
            Log.d("reading", "READING OUT OF BOUNDS (" + reading.latitude() + " " + reading.longitude() + ")");
            return true;
        }
        return false;
    }

    public int[][] getAverageZoneSignalLevels(){
        int[][] zoneAverages = new int[numZonesX][numZonesY];
        for (int x = 0; x < numZonesX; x++){
            for (int y = 0; y < numZonesY; y++){
                zoneAverages[x][y] = wifiReadingZones[x][y].averageStrength();
            }
        }
        return zoneAverages;
    }
}
