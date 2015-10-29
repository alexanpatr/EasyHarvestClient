package com.www.client;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.www.client.pm.EasyPrivacy;
import com.www.client.pm.PrivacyMechanism;

import java.io.File;
import java.io.IOException;

public class ClientActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT_DIR = SDCARD_DIR + "/Client";
    private static Context context;
    private static ConnectivityManager connectivityManager;
    public static boolean restartService = false;
    SeekBar seekBar;
    TextView statusText;
    TextView levelText;
    TextView commentText;
    public static SharedPreferences sharedPref = null;
    public static SharedPreferences.Editor editor = null;

    PrivacyMechanism pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = getClass().getName() + "@onCreate: ";

        super.onCreate(savedInstanceState);

        Log.wtf(TAG, "...");

        setContentView(R.layout.activity_client);
        ClientActivity.context = getApplicationContext();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        seekBar = (SeekBar) findViewById(R.id.levelService);
        seekBar.setOnSeekBarChangeListener(this);
        statusText = (TextView) findViewById(R.id.status);
        levelText = (TextView) findViewById(R.id.level);
        commentText = (TextView) findViewById(R.id.comment);
        Button ok = (Button) findViewById(R.id.okButton);
        Button cancel = (Button) findViewById(R.id.cancelButton);
        ok.setOnClickListener(okListener);
        cancel.setOnClickListener(cancelListener);
    }

    @Override
    protected void onStart() {
        String TAG = getClass().getName() + "@onStart: ";

        super.onStart();
        Log.wtf(TAG, "onStart: " + "...");

        if (sharedPref.getString("username", null) == null) {
            mkdirs();
            editor.putInt("serviceLevel", 0);
            editor.commit();
            statusText.setTextColor(Color.rgb(255, 0, 0));
            statusText.setText("OFF");
            commentText.setTextColor(Color.rgb(100, 100, 100));
            commentText.setText("Slide the bar right to turn the service on & set the desired level of activity...");
        } else {
            seekBar.setProgress(sharedPref.getInt("serviceLevel", 0));
            seekBar.setSecondaryProgress(sharedPref.getInt("serviceLevel", 0));
            if (sharedPref.getInt("serviceLevel", 0) > 0) {
                statusText.setTextColor(Color.rgb(0, 255, 0));
                statusText.setText("ON");
                commentText.setTextColor(Color.rgb(255, 255, 255));
                commentText.setText("The service will download tasks from the server, execute them & send the results back.");
                if (!isServiceRunning()) {
                    Log.wtf(TAG, "onStart: Restarting...");
                    startService(new Intent(ClientActivity.this, TaskService.class));
                }
            } else {
                statusText.setTextColor(Color.rgb(255, 0, 0));
                statusText.setText("OFF");
                commentText.setTextColor(Color.rgb(100, 100, 100));
                commentText.setText("Nothing to do.");
            }
        }

        /*
         * Do not comment out!
         */
        onTest();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onResume();
    }

    private void onTest() {
        String TAG = getClass().getName() + "@onTest: ";

        editor.putString("username", "emkatsom");

//        editor.putString("taskID", "1");

        /**/
//        editor.putBoolean(Globals.easy_privacy, false);
        editor.putBoolean(Globals.easy_privacy, true);

//        editor.putString("deviceID", "1234");
//
//        editor.putString(Globals.pm_id, "13");
//        editor.putInt(Globals.privacy_level, 24);
        /**/

        editor.commit();

//        startActivity(new Intent(getBaseContext(), SettingsActivity.class));

//        startActivity(new Intent(getBaseContext(), PrivacyRegionsActivity.class));
//        startActivity(new Intent(getBaseContext(), AddPrivacyRegionActivity.class));

//        startActivity(new Intent(getBaseContext(), com.www.client.pm.PrivacyMechanismsActivity.class));
//        startActivity(new Intent(getBaseContext(), com.www.client.pm.ListPrivacyMechanismsActivity.class));

    }

    private OnClickListener okListener = new OnClickListener() {
        public void onClick(View v) {
            /**/
            if (seekBar.getProgress() > 0 && sharedPref.getInt("serviceLevel", 0) == 0) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("serviceLevel", seekBar.getProgress());
                editor.commit();
                seekBar.setSecondaryProgress(seekBar.getProgress());
                enableBootReceiver();
                startService(new Intent(ClientActivity.this, TaskService.class));
            } else if (seekBar.getProgress() == 0) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("serviceLevel", seekBar.getProgress());
                editor.commit();
                seekBar.setSecondaryProgress(seekBar.getProgress());
                disableBootReceiver();
                stopService(new Intent(ClientActivity.this, TaskService.class));
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("serviceLevel", seekBar.getProgress());
                editor.commit();
                seekBar.setSecondaryProgress(seekBar.getProgress());
            }
            /**/

            /*
            if(pm != null) {
//                pm.onStart();
            } else {
                pm = new PrivacyMechanism(
                        getApplicationContext(),
                        Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")),
                        Integer.valueOf(sharedPref.getString(Globals.st_id, "0")));
//                pm.onStart();
            }
            /**/
        }
    };

    private OnClickListener cancelListener = new OnClickListener() {
        public void onClick(View v) {
            seekBar.setProgress(sharedPref.getInt("serviceLevel", 0));
            seekBar.setSecondaryProgress(sharedPref.getInt("serviceLevel", 0));
            levelText.setText(null);

            /*
            if(pm != null) {
                pm.onStop();
                pm = null;
            }
            /**/
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        int c = (int) (100 + arg0.getProgress() * 1.55);
        levelText.setTextColor(Color.rgb(c, c, c));
        levelText.setText(arg0.getProgress() + "%");
        if (arg1 == 0) {
            statusText.setTextColor(Color.rgb(255, 0, 0));
            statusText.setText("OFF");
            commentText.setTextColor(Color.rgb(100, 100, 100));
            commentText.setText("Nothing to do.");
        } else {
            statusText.setTextColor(Color.rgb(0, 255, 0));
            statusText.setText("ON");
            commentText.setTextColor(Color.rgb(255, 255, 255));
            commentText.setText("The service will download tasks from the server, execute them & send the results back.");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        int c = (int) (100 + arg0.getProgress() * 1.55);
        levelText.setTextColor(Color.rgb(c, c, c));
        levelText.setText(arg0.getProgress() + "%");
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        levelText.setText(null);
    }

    private void enableBootReceiver() {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableBootReceiver() {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static Context getContext() {
        return ClientActivity.context;
    }

    public static boolean isOnline() {
        Boolean wifiConnected, mobileConnected;
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
        if (((ClientActivity.sharedPref.getString("listPref", "Wi-Fi").equals("Any")) && (wifiConnected || mobileConnected))
                || ((ClientActivity.sharedPref.getString("listPref", "Wi-Fi").equals("Wi-Fi")) && (wifiConnected))) {
            return true;
        } else {
            return false;
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

    private void mkdirs() {
        String TAG = getClass().getName() + "@mkdirs: ";

        File clientDir = new File(CLIENT_DIR);
        File settingsDir = new File(CLIENT_DIR + "/Settings");
        File pmsDir = new File(CLIENT_DIR + "/PMs");

        Utils.delete(clientDir);
        Utils.delete(settingsDir);
        Utils.delete(pmsDir);

        if(clientDir.mkdir()) {
            Log.wtf(TAG, "Created " + clientDir.getPath());
        } else {
            Log.wtf(TAG, "Could not create Client directory.");
        }

        if(settingsDir.mkdir()) {
            Log.wtf(TAG, "Created " + settingsDir.getPath());
        } else {
            Log.wtf(TAG, "Could not create Settings directory.");
        }

        if(pmsDir.mkdir()) {
            Log.wtf(TAG, "Created " + pmsDir.getPath());
        } else {
            Log.wtf(TAG, "Could not create PMs directory.");
        }
    }
}
