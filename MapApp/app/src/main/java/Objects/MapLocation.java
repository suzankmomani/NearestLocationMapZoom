package Objects;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by suzan on 25/10/17.
 */

public class MapLocation {
    private double lat;
    private double lng;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MapLocation(String name, double lat, double lng){
        this.name=name;
        this.lat=lat;
        this.lng=lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

}
