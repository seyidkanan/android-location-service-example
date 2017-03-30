package az.kanan.android_location_service_example;

import android.location.Location;

/**
 * Created by Kanan on 3/27/2017.
 */

public class LocationEvent {

    private Location location;

    public LocationEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
