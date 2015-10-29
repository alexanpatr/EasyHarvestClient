package com.www.client;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.www.client.pm.PrivacyMechanismsActivity;
import com.www.client.pr.PrivacyRegionsActivity;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "SettingsActivity";

    public static PreferenceScreen prefScreen = null;
    public static SharedPreferences sp = null;
    public static SharedPreferences.Editor spEditor = null;

    SwitchPref privacyRegions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.wtf(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*
         * Privacy regions switch listener.
         */
        privacyRegions = (SwitchPref) findPreference(getResources().getString((R.string.privacy_regions_status)));
        privacyRegions.setOnPreferenceClickListener(this);
        privacyRegions.setOnPreferenceChangeListener(this);

        /*
         * Set preference item listeners.
         */
        findPreference(getResources().getString((R.string.privacy_mechanisms_key))).setOnPreferenceClickListener(this);

        findPreference("device_unregister").setOnPreferenceClickListener(this);

    }

    @Override
    protected void onResume() {
        //Log.wtf(TAG, "onResume");
        super.onResume();
        if (sp == null) {
            //Log.wtf(TAG, "onResume: " + "sp == null");
            sp = PreferenceManager.getDefaultSharedPreferences(this);
            spEditor = sp.edit();
            sp.registerOnSharedPreferenceChangeListener(this);
        } else {
            //Log.wtf(TAG, "onResume: " + "sp != null");
        }
        privacyRegions.setChecked(sp.getBoolean(getResources().getString((R.string.privacy_regions_status)), false));
        //this.onContentChanged();
    }

    @Override
    protected void onPause() {
        //Log.wtf(TAG, "onPause");
        super.onPause();
        if (sp != null) {
            sp.unregisterOnSharedPreferenceChangeListener(this);
            sp = null;
        }
    }/**/

    @Override
    protected void onStop() {
        //Log.wtf(TAG, "onStop");
        super.onStop();
        if (sp != null) {
            sp.unregisterOnSharedPreferenceChangeListener(this);
            sp = null;
        }
        //finish();
    }/**/

    /*@Override
    protected void onDestroy() {
        Log.wtf(TAG, "onDestroy");
        super.onDestroy();
        sp.unregisterOnSharedPreferenceChangeListener(this);
        sp = null;
    }/**/

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.wtf(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        //onResume();
    }*/


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Toast.makeText(getApplicationContext(), "onSharedPreferenceChanged: " + key, Toast.LENGTH_SHORT).show();
        Log.wtf(TAG, "onSharedPreferenceChanged: " + sharedPreferences.getAll());
        if (key.equals("listPref") || key.equals(getResources().getString((R.string.privacy_regions_status)))) {
//            ClientActivity.restartService = true;
            Toast.makeText(getApplicationContext(), "Restarting...", Toast.LENGTH_SHORT).show();
            if (isServiceRunning()) {
                Log.wtf(TAG, "onSharedPreferenceChanged: " + "Restarting service...");
                stopService(new Intent(this, TaskService.class));
                startService(new Intent(this, TaskService.class));
            }
        }
    }

    /*
     * Control action when a preference is clicked.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
//        Toast.makeText(getApplicationContext(), "onPreferenceClick: " + preference.getKey(), Toast.LENGTH_SHORT).show();

        /*
         * Launch Privacy Regions activity.
         */
        if (getResources().getString((R.string.privacy_regions_status)).equals(preference.getKey())) {
//            Toast.makeText(ClientActivity.getContext(), "onPreferenceClick: " + preference.getKey(), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, PrivacyRegionsActivity.class);
            startActivity(i);
        }

        /*
         * Launch Privacy Mechanisms activity.
         */
        if (getResources().getString((R.string.privacy_mechanisms_key)).equals(preference.getKey())) {
//            Toast.makeText(ClientActivity.getContext(), "onPreferenceClick: " + preference.getKey(), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, PrivacyMechanismsActivity.class);
            startActivity(i);
        }

        /*
         * Unregister the device.
         */
        if ("device_unregister".equals(preference.getKey())) {
            Toast.makeText(ClientActivity.getContext(), "Unregistering...", Toast.LENGTH_SHORT).show();
            String deviceID = ClientActivity.sharedPref.getString("deviceID", null);
            if (deviceID != null) {
                if (ClientActivity.isOnline()) {
                    Log.wtf(TAG, "deviceUnregister: " + "Online.");
                    String url = Globals.devs_url + "/" + deviceID + "/unregister";
                    Log.wtf(TAG, "deviceUnregister: " + "" + deviceID + "/unregister");
                    new DeviceUnregister().execute(url);
                } else {
                    Log.wtf(TAG, "deviceUnregister: " + "Offline.");
                    Toast.makeText(ClientActivity.getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ClientActivity.getContext(), "Not registered", Toast.LENGTH_SHORT).show();
            }

        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        //Toast.makeText(getApplicationContext(), "onPreferenceChange: " + preference.getKey(), Toast.LENGTH_SHORT).show();
        if ("privacyRegionsStatus".equals(preference.getKey())) {
            //Toast.makeText(getApplicationContext(), "onPreferenceChange: " + preference.getKey() + " = " + o.toString(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private class DeviceUnregister extends AsyncTask<String, Void, String> {
        private static final String TAG = "DeviceUnregister";
        private HttpClient client;

        @Override
        protected String doInBackground(String... urls) {
            Log.wtf(TAG, "doInBackground: " + "...");
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            HttpResponse response = null;
            String result = "oops";
            try {
                response = client.execute(request);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if (response != null) {
                try {
                    result = Utils.writeToString(response.getEntity().getContent());
                } catch (Exception e) {
                }
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.wtf(TAG, "onPostExecute: " + result + ".");
            if (result.equals("OK") || result.equals("Not registered")) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), TaskService.class));
                spEditor.putInt("serviceLevel", 0);
                spEditor.putString("deviceID", null);
                spEditor.putString("username", null);
                spEditor.putString("serviceStatus", "get");
                spEditor.putString("getStatus", "getTaskInfo");
                spEditor.commit();
                try {
                    FileUtils.cleanDirectory(new File(ClientActivity.CLIENT_DIR));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Oops!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TaskService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
