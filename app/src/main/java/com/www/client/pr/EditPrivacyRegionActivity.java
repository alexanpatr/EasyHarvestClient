package com.www.client.pr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.www.client.R;
import com.www.client.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class EditPrivacyRegionActivity extends Activity {
    private static final String TAG = "EditPrivacyRegionActivity";

    private JSONObject privacyRegion = null;

    private GoogleMap regionMap = null;
    private EditText region_label = null;

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_privacy_region);

        try {
            privacyRegion =  new JSONObject((String) getIntent().getSerializableExtra("privacyRegion"));
            //Log.i(TAG, privacyRegion.toString());
            setTitle(privacyRegion.getString("label"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        regionMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.region_map)).getMap();
        final View mapView = getFragmentManager().findFragmentById(R.id.region_map).getView();

        LatLngBounds.Builder bounds;
        bounds = new LatLngBounds.Builder();

        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                    LatLngBounds bounds = null;
                    try {
                        bounds = new LatLngBounds.Builder()
                                .include(new LatLng(privacyRegion.getDouble("ne_lat"), privacyRegion.getDouble("ne_lng")))
                                .include(new LatLng(privacyRegion.getDouble("sw_lat"), privacyRegion.getDouble("sw_lng")))
                                .build();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    regionMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                }
            });
        }

        region_label = (EditText) findViewById(R.id.region_label);
        try {
            region_label.setText(privacyRegion.getString("label"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //region_label.setImeActionLabel("OK", EditorInfo.IME_ACTION_DONE);
        region_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                region_label.setError(null);
            }
        });/**/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_privacy_region, menu);
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
            editPrivacyRegion();
            return true;
        }/**/
        if (id == R.id.action_cancel) {
            //Toast.makeText(getApplicationContext(), "onOptionsItemSelected: " + "CANCEL", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }/**/
        return super.onOptionsItemSelected(item);
    }

    public void editPrivacyRegion() {
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
                //Log.i(TAG, "editPrivacyRegion: " + region.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<JSONObject> privacyRegions = new ArrayList<JSONObject>();
            privacyRegions = Utils.fileToJSON(new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));

            int i = 0;
            for (JSONObject jsonObject : privacyRegions) {
                try {
                    if (jsonObject.getString("id").equals(privacyRegion.getString("id"))
                            && jsonObject.getString("label").equals(privacyRegion.getString("label"))) {
                        //Toast.makeText(getApplicationContext(), jsonObject.getString("label"), Toast.LENGTH_SHORT).show();
                        privacyRegions.set(i, region);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                i++;
            }
            Utils.overwriteJSONtoFile(privacyRegions, new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));

            try {
                Toast.makeText(getApplicationContext(), "\"" + privacyRegion.getString("label") + "\" " + "has been edited.", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            finish();
            /*Intent intent = new Intent(this, ViewPrivacyRegionActivity.class);
            intent.putExtra("privacyRegion", region.toString());
            startActivity(intent);/**/
        }
    }
}
