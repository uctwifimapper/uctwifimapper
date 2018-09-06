import com.google.gson.annotations.SerializedName;
import org.postgresql.geometric.PGpoint;

import java.math.BigInteger;

public class SignalStrength {

    @SerializedName("bssid")
    private String bssid; //Unique mac address of wireless router
    @SerializedName("signalStrength")
    private int signalStrength; //level of signal strength
    @SerializedName("location")
    private PGpoint location; //location of the router
    @SerializedName("timestamp")
    private long timestamp;

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SignalStrength{" +
                "bssid='" + bssid + '\'' +
                ", signalStrength=" + signalStrength +
                ", location=" + location.getValue() +
                ", timestamp=" + timestamp +
                '}';
    }
}
