package com.www.client.pm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.www.client.Globals;
import com.www.client.R;


public class PrivacyMechanismsActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "PrivacyMechanismsActivity";

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    private static ConnectivityManager cm;

    public static SharedPreferences sp = null;
    public static SharedPreferences.Editor spEditor = null;

    SeekBar seekBar;
    TextView statusText;
    TextView levelText;
    TextView commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_mechanisms);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();

        statusText = (TextView) findViewById(R.id.status);
        statusText.setText("LOW");
        statusText.setTextColor(Color.rgb(255, 0, 0));
        levelText = (TextView) findViewById(R.id.level);
        commentText = (TextView) findViewById(R.id.comment);

        seekBar = (SeekBar) findViewById(R.id.level_bar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(sp.getInt(Globals.privacy_level, 0));
        seekBar.setSecondaryProgress(sp.getInt(Globals.privacy_level, 0));

        levelText.setText("");

        /*
         * Buttons
         */
        findViewById(R.id.ok_btn).setOnClickListener(okListener);
        findViewById(R.id.cancel_btn).setOnClickListener(cancelListener);;

        // install
//        Button getListBtn = (Button)findViewById(R.id.get_list_btn);
//        getListBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                startActivity(new Intent(getBaseContext(), AddPrivacyRegionActivity.class));
//                Toast.makeText(getApplicationContext(), "list", Toast.LENGTH_SHORT).show();
//            }
//        });

        // view installed
//        Button viewBtn = (Button)findViewById(R.id.view_btn);
//        viewBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                startActivity(new Intent(getBaseContext(), AddPrivacyRegionActivity.class));
//                Toast.makeText(getApplicationContext(), "view", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    /*
     * Action bar menu methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_privacy_mechanisms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.privacy_mechanisms_get_list) {
//            Toast.makeText(getApplicationContext(), "list", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ListPrivacyMechanismsActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.privacy_mechanisms_view_installed) {
//            Toast.makeText(getApplicationContext(), "in", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ViewPrivacyMechanismActivity.class);
            i.putExtra("intent", "view");
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Seekbar methods
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int r = (255 * (100 - progress)) / 100;
        int g = (255 * progress) / 100;
        int b = 0;

        levelText.setTextColor(Color.rgb(r, g, b));
        levelText.setText(progress + "%");

        statusText.setTextColor(Color.rgb(r, g, b));

        if (progress < 33) {
            statusText.setText("LOW");
        } else if (progress >= 33 && progress <66) {
            statusText.setText("MEDIUM");
        } else {
            statusText.setText("HIGH");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        levelText.setText("");
    }

    private void updateContentView(int i) {

    }

    /*
     * Buttons
     */
    private View.OnClickListener okListener = new View.OnClickListener() {

        // OK button
        public void onClick(View v) {
            String TAG = getClass().getName() + "@onClick: ";

            seekBar.setSecondaryProgress(seekBar.getProgress());
            spEditor.putInt(Globals.privacy_level, seekBar.getProgress());
            spEditor.commit();

            Log.wtf(TAG, Globals.privacy_level + " -> " + seekBar.getProgress());
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        // Cancel button
        public void onClick(View v) {
            seekBar.setProgress(sp.getInt(Globals.privacy_level, 0));
            seekBar.setSecondaryProgress(sp.getInt(Globals.privacy_level, 0));
            levelText.setText("");
        }
    };
}
