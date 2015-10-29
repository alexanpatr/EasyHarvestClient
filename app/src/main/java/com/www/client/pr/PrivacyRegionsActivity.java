package com.www.client.pr;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.www.client.ClientActivity;
import com.www.client.ItemArrayAdapter;
import com.www.client.R;
import com.www.client.TaskService;
import com.www.client.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class PrivacyRegionsActivity extends Activity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {
    private static final String TAG = "PrivacyRegionsActivity";

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    public static SharedPreferences sp = null;
    public static SharedPreferences.Editor spEditor = null;

    Switch actionBarSwitch;

    ArrayList<JSONObject> privacyRegions = null;

    ListView listView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_regions);
        setTitle(getResources().getString((R.string.privacy_regions_title)));

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();

        Button addNewButton = (Button)findViewById(R.id.add_new);
        addNewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startActivity(new Intent(getBaseContext(), AddPrivacyRegionActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        //Log.i(TAG, "onResume: ...");
        super.onResume();
        loadList();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_privacy_regions, menu);

        actionBarSwitch = (Switch)menu.findItem(R.id.action_bar_switch).getActionView().findViewById(R.id.switch_button);
        actionBarSwitch.setChecked(sp.getBoolean(getResources().getString((R.string.privacy_regions_status)), false));
        actionBarSwitch.setOnCheckedChangeListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.switch_button:
                Toast.makeText(ClientActivity.getContext(), "onOptionsItemSelected: " + item.toString(), Toast.LENGTH_SHORT).show();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //Toast.makeText(getApplicationContext(), "onCheckedChanged: " + compoundButton, Toast.LENGTH_SHORT).show();
        if (compoundButton == actionBarSwitch) {
            //Toast.makeText(getApplicationContext(), "onCheckedChanged: " + compoundButton + " = " + b, Toast.LENGTH_SHORT).show();
            spEditor.putBoolean(getResources().getString((R.string.privacy_regions_status)), b);
            spEditor.commit();
            //Log.i(TAG, "onCheckedChanged: " + sp.getAll());
            //restart service
            //Toast.makeText(getApplicationContext(), "Restarting...", Toast.LENGTH_SHORT).show();
            if(isServiceRunning()) {
                Log.i(TAG, "onCheckedChanged: " + "Restarting service...");
                stopService(new Intent(this, TaskService.class));
                startService(new Intent(this, TaskService.class));
            }/**/
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        JSONObject choice = privacyRegions.get(i);
        //Toast.makeText(getApplicationContext(), choice.toString(), Toast.LENGTH_SHORT).show();
        //Log.i(TAG, choice.toString());
        Intent intent = new Intent(this, ViewPrivacyRegionActivity.class);
        intent.putExtra("privacyRegion", choice.toString());
        startActivity(intent);
    }

    private void loadList() {
        //Log.i(TAG, "loadList: ... ");
        privacyRegions = Utils.fileToJSON(new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));
        textView = (TextView) findViewById(R.id.info);
        listView = (ListView) findViewById(R.id.list);
        //Log.i(TAG, "loadList: " + privacyRegions.toString());

        if (privacyRegions.isEmpty()) {
            //Log.i(TAG, "privacyRegions is empty");
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
        else {
            //Log.i(TAG, "privacyRegions not empty");
            textView.setVisibility(View.GONE);
            /*for (JSONObject privacyRegion : privacyRegions) {
                Log.i(TAG, "loadList: " + privacyRegion.toString());
            }/**/

            listView = (ListView) findViewById(R.id.list);

            ItemArrayAdapter adapter = new ItemArrayAdapter(this, R.layout.item, privacyRegions);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TaskService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
