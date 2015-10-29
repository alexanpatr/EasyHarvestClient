package com.www.client.pm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.www.client.ClientActivity;
import com.www.client.Globals;
import com.www.client.R;
import com.www.client.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListPrivacyMechanismsActivity extends Activity implements AdapterView.OnItemClickListener {

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    TextView textView;

    ListView listView;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = ListPrivacyMechanismsActivity.class.getName() + "@onCreate: ";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_privacy_mechanisms);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();

//        getList();
    }

    @Override
    protected void onResume() {
        //Log.i(TAG, "onResume: ...");
        super.onResume();
        getList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_privacy_mechanisms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.get_privacy_mechanism_action_refresh) {
//            Toast.makeText(getApplicationContext(), "refresh", Toast.LENGTH_SHORT).show();

            getList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String TAG = getClass().getName() + "@onItemClick: ";
        JSONObject choice = null;
        try {
            choice = new JSONObject(list.get(position));

//            Toast.makeText(getApplicationContext(), choice.toString(), Toast.LENGTH_SHORT).show();
//            Log.wtf(TAG, choice.toString());

            Intent intent = new Intent(this, ViewPrivacyMechanismActivity.class);
            intent.putExtra("intent", "get");

            intent.putExtra("id", choice.getJSONArray("id").getString(0));
            intent.putExtra("name", choice.getJSONArray("name").getString(0));
//          class name
            intent.putExtra("class", choice.getJSONArray("class").getString(0));
//
            intent.putExtra("version", choice.getJSONArray("version").getString(0));
            intent.putExtra("description", choice.getJSONArray("description").getString(0));
            intent.putExtra("user", choice.getJSONArray("user").getString(0));
            intent.putExtra("date", choice.getJSONArray("date").getString(0));
            intent.putExtra("time", choice.getJSONArray("time").getString(0));
            intent.putExtra("size", choice.getJSONArray("size").getString(0));

            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getList() {
        String TAG = getClass().getName() + "@getList: ";
//        Toast.makeText(getApplicationContext(), "Getting list...", Toast.LENGTH_SHORT).show();

        listView = (ListView) findViewById(R.id.listView);
        listView.setVisibility(View.INVISIBLE);
        list = new ArrayList<>();

        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Please wait...");
        textView.setVisibility(View.VISIBLE);

        String stID = sp.getString("taskID", null);
        String deviceID = sp.getString("deviceID", null);

        if (deviceID != null && stID != null) {
            if (ClientActivity.isOnline()) {
                String url = Globals.pms_url + "/getlist/" + stID + "/" + deviceID;
                Log.wtf(TAG, url);
//                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
                new GetList().execute(url);
            } else {
                Log.wtf(TAG, "Offline.");
//                Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
                textView.setText("No network connection");
            }
        } else {
            Log.wtf(TAG, "Not registered yet");
//            Toast.makeText(getApplicationContext(), "Not registered yet", Toast.LENGTH_SHORT).show();
            textView.setText("Not registered yet");
        }
    }

    private class GetList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String TAG = GetList.class.getName() + "@doInBackground: ";

            Log.wtf(TAG, "...");

            HttpClient client  = new DefaultHttpClient();
            HttpGet request = new HttpGet(params[0]);
            HttpResponse response = null;
            String result = "Oops!";

            try {
                response = client.execute(request);
                if (response != null) {
                    result = Utils.writeToString(response.getEntity().getContent());
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return result;
        }

        protected void onPostExecute(String result) {
            String TAG = GetList.class.getName() + "@onPostExecute: ";

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            Log.wtf(TAG, result);

            boolean error = true;
            try {
                if (list.size() > 0) {
                    JSONObject o = new JSONObject(list.get(0));
                    o.getJSONArray("Error").getString(0);
                }
            } catch (JSONException e) {
                error = false;
            }


            if ("Oops!".equals(result)) {
                textView.setText("Oops, something went wrong");
            } else if (list.size() == 0) {
                textView.setText("There are no privacy mechanisms available");

            } else if (error) {
                textView.setText("Not registered yet");
            } else {
                textView.setVisibility(View.INVISIBLE);

                /*
                 * List
                 */
                PrivacyMechanismArrayAdapter adapter = new PrivacyMechanismArrayAdapter(getApplicationContext(), R.layout.item, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(ListPrivacyMechanismsActivity.this);
                listView.setVisibility(View.VISIBLE);
            }
        }
    }

}
