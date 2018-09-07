import com.google.gson.annotations.SerializedName;
import org.postgresql.geometric.PGpoint;

/*

/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

* Business model for the access point
* This class represents an access point
* */

public class AccessPoint {
    @SerializedName("name")
    private String name; //name of point e.g computer science level 1
    @SerializedName("bssid")
    private String bssid; //Unique mac address of wireless router
    @SerializedName("ssid")
    private String ssid; //Network identifier e.g eduroam
    @SerializedName("location")
    private PGpoint location; //location of the router
    @SerializedName("linkSpeed")
    private int linkSpeed;
    @SerializedName("timestamp")
    private long timestamp; //unix timestamp

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public PGpoint getLocation() {
        return location;
    }

    public void setLocation(PGpoint location) {
        this.location = location;
    }

    public int getLinkSpeed() {
        return linkSpeed;
    }

    public void setLinkSpeed(int linkSpeed) {
        this.linkSpeed = linkSpeed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
