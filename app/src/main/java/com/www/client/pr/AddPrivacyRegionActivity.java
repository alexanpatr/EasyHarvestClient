package com.www.client.pr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.www.client.R;
import com.www.client.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddPrivacyRegionActivity extends Activity {
    private static final String TAG = "AddPrivacyRegionActivity";

    private GoogleMap regionMap = null;
    private EditText region_label = null;

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_privacy_region);

        GoogleMapOptions mapOptions = new GoogleMapOptions();
        regionMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.region_map)).getMap();

        region_label = (EditText) findViewById(R.id.region_label);
        //region_label.setImeActionLabel("OK", EditorInfo.IME_ACTION_DONE);
        region_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                region_label.setError(null);
            }
        });/**/
        /*region_label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                region_label.setError(null);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        /*region_label.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addPrivacyRegion();
                }
                return false;
            }
        });/**/
        //region_label.setImeActionLabel("OK", KeyEvent.KEYCODE_ENTER);

        final TextView info = (TextView) findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                info.setVisibility(View.GONE);
            }
        });/**/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_privacy_region, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            //Toast.makeText(getApplicationContext(), "onOptionsItemSelected: " + "OK", Toast.LENGTH_SHORT).show();
            addPrivacyRegion();

            return true;
        }/**/
        if (id == R.id.action_cancel) {
            //Toast.makeText(getApplicationContext(), "onOptionsItemSelected: " + "CANCEL", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }/**/
        return super.onOptionsItemSelected(item);
    }

    public void addPrivacyRegion() {
        if(region_label.getText().toString().trim().equals("")) {
            region_label.setError("Region label is required!");
        }
        else {
            region_label.setError(null);
            LatLngBounds bounds = regionMap.getProjection().getVisibleRegion().latLngBounds;
            String label = region_label.getText().toString();

            /*Toast.makeText(getApplicationContext(), region_label.getText().toString() + "\n"
                    + bounds.northeast.latitude + " - " + bounds.northeast.longitude + "\n"
                    + bounds.southwest.latitude + " - " + bounds.southwest.longitude, Toast.LENGTH_SHORT).show();/**/

            JSONObject region = new JSONObject();
            try {
                region.put("label", label);
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
                region.put("id", sdf.format(new Date()));
                region.put("ne_lat", bounds.northeast.latitude);
                region.put("ne_lng", bounds.northeast.longitude);
                region.put("sw_lat", bounds.southwest.latitude);
                region.put("sw_lng", bounds.southwest.longitude);
                Log.i(TAG, "addPrivacyRegion: " + region.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.writeJSONtoFile(region, new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));

            try {
                Toast.makeText(getApplicationContext(), "\"" + region.getString("label") + "\" " + " has been added.", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /**/
            startActivity(new Intent(getBaseContext(), PrivacyRegionsActivity.class));
        }
    }

}
