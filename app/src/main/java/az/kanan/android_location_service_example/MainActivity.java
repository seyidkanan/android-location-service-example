package az.kanan.android_location_service_example;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
//        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    LocationManager lm;
    GoogleApiClient mGoogleApiClient;

    String mLastUpdateTime;
    Location mCurrentLocation;

    Button btn, goButton;
    TextView textView, textView2, textView3;
    RadioGroup radioButtonsGroup;

    SharedSaver sharedSaver;
    boolean isGoButtonSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

//        if (!isLocationEnabled()) {
//            showAlertDialogForLocation();
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 200);
        }


        sharedSaver = new SharedSaver(this);

        if (isLocationEnabled()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }
        btn = (Button) findViewById(R.id.updateLoc);
        goButton = (Button) findViewById(R.id.button2);

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);

        radioButtonsGroup = (RadioGroup) findViewById(R.id.radioButtonGroup);

        showProvidersEnable();

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (radioButtonsGroup.getCheckedRadioButtonId()) {
                    case R.id.radioButton:
                        if (!isOnline()) {
                            showLongToast("For this need internet connetion, TURN ON it");
                        }
                        startMyService(1);
                        break;
                    case R.id.radioButton2:
                        if (!isLocationEnabled()) {
                            showLongToast("For this need GPS connetion, TURN ON it");
                        }
                        startMyService(0);
                        break;
                }
                isGoButtonSelected = true;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_DENIED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, 200);
                    }
                } else {
                    if (isLocationEnabled()) {
                        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLastLocation != null) {
                            mCurrentLocation = mLastLocation;
                        }
                        updateUI();
                    } else {
                        showAlertDialogForLocation();
                    }
                }

            }
        });

    }

    private void startMyService(int service) {//0 is GPS, 1 is Network
        stopMyService();
        sharedSaver.setTypeOfProvider(service);
        Intent intent = new Intent(this, MyLocationService.class);
        startService(intent);
        showToast("starting location service");
    }

    public boolean isLocationEnabled() {
        int locationMode;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void updateUI() {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        textView.setText("Latitude= " + String.valueOf(mCurrentLocation.getLatitude()) +
                "\nLongitude= " + String.valueOf(mCurrentLocation.getLongitude()));
        textView2.setText(mLastUpdateTime);
        showToast("UI updated");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
//                boolean isLocationPermissionAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                        && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        stopMyService();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            showToast("Google Api Client starting connect client");
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            showToast("Google Api Client connect disconnecting");
        }
        super.onStop();
    }


    protected void showProvidersEnable() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textView3.setText("GPS Provider is enable? = " + lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                + "\n" + "Network Provider is enable? = " + lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mGoogleApiClient != null) {
            showToast("Google Api Client conected");
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        showToast("Google Api Client Connection Suspended " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Google Api Client Connection Failed " + connectionResult.getErrorMessage());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                showToast("OnPause Google Api Client connected");
            } else {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                showToast("OnResume Google Api Client connected");
            } else {
                mGoogleApiClient.connect();
            }
        }
    }

    private void showAlertDialogForLocation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("GPS is turn off, turn on it");
        dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationEvent event) {
        mCurrentLocation = event.getLocation();
        updateUI();
    }


    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void stopMyService() {
        showToast("Stoping current service");
        Intent intent = new Intent(this, MyLocationService.class);
        stopService(intent);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
