import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

public class AccessPoint {
    private String bssid;
    private String ssid;
    private PGpoint location;
    private int linkSpeed;

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
}
