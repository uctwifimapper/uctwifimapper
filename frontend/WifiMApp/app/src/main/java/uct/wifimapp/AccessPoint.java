package uct.wifimapp;

import com.google.gson.annotations.SerializedName;

public class AccessPoint {
    @SerializedName("name")
    public String name;
    @SerializedName("bssid")
    public String bssid;
    @SerializedName("ssid")
    public String ssid;
    @SerializedName("location")
    public Location location;
    @SerializedName("linkSpeed")
    public int linkSpeed;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getLinkSpeed() {
        return linkSpeed;
    }

    public void setLinkSpeed(int linkSpeed) {
        this.linkSpeed = linkSpeed;
    }

    @Override
    public String toString() {
        return "AccessPoint{" +
                "name='" + name + '\'' +
                ", bssid='" + bssid + '\'' +
                ", ssid='" + ssid + '\'' +
                ", location=" + location +
                ", linkSpeed=" + linkSpeed +
                '}';
    }
}
