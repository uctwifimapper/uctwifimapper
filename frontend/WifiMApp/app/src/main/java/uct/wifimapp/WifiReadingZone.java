package uct.wifimapp;

import java.util.ArrayList;

/**
 * A collection of all the wifi readings taken in a single map segment.
 */
public class WifiReadingZone {

    ArrayList<WifiReading> wifiReadings;

    public WifiReadingZone(){
        wifiReadings = new ArrayList<WifiReading>();
    }

    public void addWifiReading(WifiReading reading){
        wifiReadings.add(reading);
    }

    public float averageStrength(){
        float total = 0;

        for (WifiReading reading : wifiReadings){
            total += (float)reading.strength();
        }

        return total / (float)wifiReadings.size();
    }

    public void reset(){
        wifiReadings.clear();
    }

}
