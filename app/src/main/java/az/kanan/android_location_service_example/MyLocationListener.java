package az.kanan.android_location_service_example;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

/**
 * Created by Kanan on 3/27/2017.
 */

class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        Log.e("kanan", "dfghf");
    }

}
