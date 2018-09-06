import org.postgresql.geometric.PGpoint;

import java.math.BigInteger;

public class SignalStrength {

    private String bssid; //Unique mac address of wireless router
    private int signalStrength; //level of signal strength
    private PGpoint location; //location of the router
    private int timestamp;

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public PGpoint getLocation() {
        return location;
    }

    public void setLocation(PGpoint location) {
        this.location = location;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
