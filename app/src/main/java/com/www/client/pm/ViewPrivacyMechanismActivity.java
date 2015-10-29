package com.www.client.pm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.www.client.ClientActivity;
import com.www.client.Globals;
import com.www.client.R;
import com.www.client.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ViewPrivacyMechanismActivity extends Activity {

    String sdcard = Environment.getExternalStorageDirectory().getPath();
    String clientDir = sdcard + "/Client";
    String pmsDir = clientDir + "/PMs";

    SharedPreferences sp = null;
    SharedPreferences.Editor spEditor = null;

    String parentActivity = "";

    TextView textView;
    Button button;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = ViewPrivacyMechanismActivity.class.getName() + "@onCreate: ";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_privacy_mechanism);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();

        final Intent i = getIntent();

        button = (Button)findViewById(R.id.button);
        button.setText("Install");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setEnabled(false);
                if("Install".equals(button.getText())) {
                    button.setText("Installing...");

                    Toast.makeText(getApplicationContext(), "Installing...", Toast.LENGTH_SHORT).show();

                    /*
                     * Remove previous PM
                     */
                    if(sp.getString("pmID", null) != null) {
                        removePM(sp.getString("pmID", null));
                    }

                    spEditor.putString("pmID", i.getStringExtra("id"));
                    spEditor.putString("pmName", i.getStringExtra("name"));
                    spEditor.putString("pmVersion", i.getStringExtra("version"));
//
//                  class name
//
                    spEditor.putString(Globals.pm_clss, i.getStringExtra("class"));
//
//                    st id
//
                    spEditor.putString(Globals.pm_st_id, sp.getString(Globals.st_id, "0"));
//
//
//
                    spEditor.putString("pmDescription", i.getStringExtra("description"));
                    spEditor.putString("pmUser", i.getStringExtra("user"));
                    spEditor.putString("pmDate", i.getStringExtra("date"));
                    spEditor.putString("pmTime", i.getStringExtra("time"));
                    spEditor.putString("pmSize", i.getStringExtra("size"));
                    spEditor.commit();

                    getPM();

//                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
//                    button.setText("Uninstall");
                } else {
                    button.setText("Uninstalling...");
                    Toast.makeText(getApplicationContext(), "Uninstalling...", Toast.LENGTH_SHORT).show();

                    removePM(sp.getString("pmID", null));

                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
//                    button.setText("Install");

//                    returnToParent();
                    finish();
                }
//                button.setEnabled(true);
            }
        });

        if("get".equals(i.getStringExtra("intent"))) {
            // Came here from the list

            parentActivity = "ListPrivacyMechanismsActivity";

            textView = (TextView) findViewById(R.id.message);
            textView.setVisibility(View.INVISIBLE);

            if(i.getStringExtra("id").equals(sp.getString("pmID", null))) {
                button.setText("Uninstall");
            }

            textView = (TextView) findViewById(R.id.name);
            textView.setText(i.getStringExtra("name"));
            setTitle(i.getStringExtra("name"));

            textView = (TextView) findViewById(R.id.version);
            textView.setText("version " + i.getStringExtra("version") + ".0");

            textView = (TextView) findViewById(R.id.user);
            textView.setText("by " + i.getStringExtra("user"));

            textView = (TextView) findViewById(R.id.description);
            textView.setText(i.getStringExtra("description"));

            textView = (TextView) findViewById(R.id.date_time);
            textView.setText(i.getStringExtra("date") + "\n" + i.getStringExtra("time"));

            textView = (TextView) findViewById(R.id.size);
//            textView.setText(i.getStringExtra("size") + "b");
            textView.setText(String.format( "%.2f", Float.valueOf(i.getStringExtra("size"))  /1000) + " KB");

            textView = (TextView) findViewById(R.id.id);
            textView.setText(i.getStringExtra("id"));
        } else {
            // Came here from the PM settings

            parentActivity = "PrivacyMechanismsActivity";

            if(sp.getString("pmID", null) != null) {
                textView = (TextView) findViewById(R.id.message);
                textView.setVisibility(View.INVISIBLE);

                button.setText("Uninstall");

                textView = (TextView) findViewById(R.id.name);
                textView.setText(sp.getString("pmName", ""));
                setTitle(sp.getString("pmName", ""));

                textView = (TextView) findViewById(R.id.version);
                textView.setText("version " + sp.getString("pmVersion", "") + ".0");

                textView = (TextView) findViewById(R.id.user);
                textView.setText("by " + sp.getString("pmUser", ""));

                textView = (TextView) findViewById(R.id.description);
                textView.setText(sp.getString("pmDescription", ""));

                textView = (TextView) findViewById(R.id.date_time);
                textView.setText(sp.getString("pmDate", "") + "\n" + sp.getString("pm_time", ""));

                textView = (TextView) findViewById(R.id.size);
                textView.setText(String.format( "%.2f", Float.valueOf(sp.getString("pmSize", "0"))/1000) + " KB");

                textView = (TextView) findViewById(R.id.id);
                textView.setText(sp.getString("pmID", ""));
            } else {
                scrollView = (ScrollView) findViewById(R.id.content);
                scrollView.setVisibility(View.INVISIBLE);

                textView = (TextView) findViewById(R.id.message);
                textView.setText("No privacy mechanism installed");
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_view_privacy_mechanism, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Respond to the action bar's Up/Home button
        if (id == android.R.id.home) {
            returnToParent();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean returnToParent() {
        Intent i = null;
        try {
            i = new Intent(this, Class.forName("com.www.client.pm." + parentActivity));
            startActivity(i);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getPM() {
        String TAG = ViewPrivacyMechanismActivity.class.getName() + "@getPM: ";
//        Toast.makeText(getApplicationContext(), "Getting list...", Toast.LENGTH_SHORT).show();

        String deviceID = sp.getString("deviceID", null);
        String pmID = sp.getString("pmID", null);

        if (deviceID != null && pmID != null) {
            if (ClientActivity.isOnline()) {
                String url = Globals.pms_url + "/" + pmID + "/getbin/" + deviceID;
                Log.wtf(TAG, url);
//                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
                new GetPM().execute(url);
            } else {
                Log.wtf(TAG, "Offline.");
                Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.wtf(TAG, "Not registered yet");
            Toast.makeText(getApplicationContext(), "Not registered yet", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetPM extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String TAG = GetPM.class.getName() + "@doInBackground: ";

            Log.wtf(TAG, "...");

            HttpClient client  = new DefaultHttpClient();
            HttpGet request = new HttpGet(params[0]);
            HttpResponse response = null;
            String result = "Oops!";

            try {
                response = client.execute(request);
                if (response != null) {
                    String dir = pmsDir + "/" + sp.getString("pmID", null);
                    Log.wtf(TAG, dir);
                    new File(dir).mkdir();
                    String url = dir + "/" + sp.getString("pmID", null) + ".zip";
                    Log.wtf(TAG, url);
                    Utils.writeToFile(response.getEntity().getContent(), url);
                    Log.wtf(TAG, Long.toString(new File(url).length()) + " == " + sp.getString("pmSize", null));
                    if(new File(url).length() == Long.valueOf(sp.getString("pmSize", null))) {
                        result = "OK";
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return result;
        }

        protected void onPostExecute(String result) {
            String TAG = GetPM.class.getName() + "@onPostExecute: ";

            Log.wtf(TAG, result);

            if(!"OK".equals(result)) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                removePM(sp.getString("pmID", null));
                button.setText("Install");
                button.setEnabled(true);
            } else {
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
//                returnToParent();
                finish();
            }
        }
    }

    private void removePM(String id) {
        Utils.delete(new File(pmsDir + "/" + id.toString()));

        spEditor.putString("pmID", null);
        spEditor.putString("pmName", null);
        spEditor.putString("pmVersion", null);
//
//        class name
        spEditor.putString(Globals.pm_clss, null);
//
//        sensing task id
        spEditor.putString(Globals.pm_st_id, null);
//
        spEditor.putString("pmDescription", null);
        spEditor.putString("pmUser", null);
        spEditor.putString("pmDate", null);
        spEditor.putString("pmTime", null);
        spEditor.putString("pmSize", null);
        spEditor.commit();
    }

}
