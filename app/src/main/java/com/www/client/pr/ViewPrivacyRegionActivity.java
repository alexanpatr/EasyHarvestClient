package com.www.client.pr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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
import java.util.ArrayList;


public class ViewPrivacyRegionActivity extends Activity {
    private static final String TAG = "ViewPrivacyRegionActivity";

    private JSONObject privacyRegion = null;

    private GoogleMap regionMap = null;

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_privacy_region);

        try {
            privacyRegion =  new JSONObject((String) getIntent().getSerializableExtra("privacyRegion"));
            //Log.i(TAG, privacyRegion.toString());
            setTitle(privacyRegion.getString("label"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        regionMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.region_map)).getMap();
        final View mapView = getFragmentManager().findFragmentById(R.id.region_map).getView();

        regionMap.getUiSettings().setZoomControlsEnabled(false);
        regionMap.getUiSettings().setCompassEnabled(false);
        regionMap.getUiSettings().setAllGesturesEnabled(false);
        regionMap.getUiSettings().setMyLocationButtonEnabled(false);/**/

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_privacy_region, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditPrivacyRegionActivity.class);
            intent.putExtra("privacyRegion", privacyRegion.toString());
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete) {
            //Toast.makeText(getApplicationContext(), "onOptionsItemSelected: " + "DELETE", Toast.LENGTH_SHORT).show();
            ArrayList<JSONObject> privacyRegions = new ArrayList<JSONObject>();
            privacyRegions = Utils.fileToJSON(new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));

            for (JSONObject jsonObject : privacyRegions) {
                try {
                    if (jsonObject.getString("id").equals(privacyRegion.getString("id"))
                            && jsonObject.getString("label").equals(privacyRegion.getString("label"))) {
                        //Toast.makeText(getApplicationContext(), jsonObject.getString("label"), Toast.LENGTH_SHORT).show();
                        privacyRegions.remove(jsonObject);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Utils.overwriteJSONtoFile(privacyRegions, new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));

            try {
                Toast.makeText(getApplicationContext(), "\"" + privacyRegion.getString("label") + "\" " + " has been deleted.", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
