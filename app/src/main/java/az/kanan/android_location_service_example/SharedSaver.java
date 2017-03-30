package az.kanan.android_location_service_example;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Kanan on 10/05/2015.
 */
public class SharedSaver {
    private SharedPreferences sharedPreferences;
    private Context context;


    public SharedSaver(Context context) {
        this.context = context;
        setUpShared();
    }


    private void setUpShared() {
        sharedPreferences = context.getSharedPreferences("LocationApp", Context.MODE_PRIVATE);
    }

    public void setIntElement(String key, int val) {
        sharedPreferences.edit().putInt(key, val).apply();
    }

    public Integer getIntElement(String key, int defaultVal) {
        return sharedPreferences.getInt(key, defaultVal);
    }

    public void setStringElement(String key, String val) {
        sharedPreferences.edit().putString(key, val).apply();
    }

    public String getStringElement(String key, String defaultVal) {
        return sharedPreferences.getString(key, defaultVal);
    }

    public void setTypeOfProvider(int type) {
        setIntElement("typeOfProvider", type);
    }

    public Integer getTypeOfProvider() {
        return getIntElement("typeOfProvider", 0);
    }


}
