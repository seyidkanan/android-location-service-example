package az.kanan.android_location_service_example;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Kanan on 3/27/2017.
 */

public class MyLocationService extends Service {

    SharedSaver sharedSaver;

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            EventBus.getDefault().post(new LocationEvent(location));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " provider is disable", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " provider is enable", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getApplicationContext(), provider + " provider status changed, " + status, Toast.LENGTH_SHORT).show();
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();

        sharedSaver = new SharedSaver(getApplicationContext());

        int type = sharedSaver.getTypeOfProvider();//0 is GPS, 1 is Network

        switch (type) {
            case 0:
                setGPSProvider();
                break;
            case 1:
                setNetworkProvider();
                break;
            default:
                setGPSProvider();
                break;
        }


        Log.e("kanan", "gps enable = " + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                + " network enable = " + mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    @Override
    public void onDestroy() {
//        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
//                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                    Toast.makeText(getApplicationContext(), "fail to remove location listners", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private void setNetworkProvider() {
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
            Toast.makeText(getApplicationContext(), "NETWORK PROVIDER is turning on", Toast.LENGTH_SHORT).show();
        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
            Toast.makeText(getApplicationContext(), "NETWORK PROVIDER is fail to request location update, ignore", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            Toast.makeText(getApplicationContext(), "NETWORK PROVIDER does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void setGPSProvider() {
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
            Toast.makeText(getApplicationContext(), "GPS PROVIDER is turning on", Toast.LENGTH_SHORT).show();
        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
            Toast.makeText(getApplicationContext(), "GPS PROVIDER is fail to request location update, ignore", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            Toast.makeText(getApplicationContext(), "GPS PROVIDER does not exist", Toast.LENGTH_SHORT).show();
        }
    }

}
