package com.www.client.pr;

import java.util.*;
import java.io.*;

import android.content.Context;
import android.util.*;

@SuppressWarnings("unchecked")
public class Demo {

    private Context context;
    private int pref;
    private List <Object> state;
    private List<Map<String, String>> peers;

    public void onStart (Context c, int i, ObjectInputStream ois) throws Exception {
        String TAG =  getClass().getName() + "@onStart: ";

        Log.wtf(TAG, "...");

//        get application context
        context = c;

//        get privacy preferences
        pref = i;

//        restore state
        if (ois != null) {
            Log.wtf(TAG, "Restoring state...");
            state = (List<Object>) ois.readObject();
        } else {
            Log.wtf(TAG, "Initializing state...");
            state = new ArrayList<>();
        }

        peers = new ArrayList<>();

    }

    public void onStop () {
        String TAG =  getClass().getName() + "@onStop: ";

        Log.wtf(TAG, "...");

    }

    public void onPreferenceChanged (int i) {
        String TAG =  getClass().getName() + "@onPreferenceChanged: ";

        Log.wtf(TAG, "" + i);

        pref = i;

    }

    public boolean saveState (ObjectOutputStream oos) {
        String TAG =  getClass().getName() + "@saveState: ";

        Log.wtf(TAG, "...");

//        s.writeObject(state);

        return false;

    }

    public int processData (ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String TAG =  getClass().getName() + "@processData: ";

        Log.wtf(TAG, "...");

//        get list of data
        List <Object> list = (List<Object>) ois.readObject();

//        edit each data entry of the data list
        for (Object o : list) {

            Map data = (Map) o;
            data.put("device", data.get("device"));
            data.put("task", data.get("task"));
            data.put("sensor", data.get("sensor"));
            data.put("values", data.get("values"));

            float min = -1;
            float max = 1;

            Random r = new Random();
            float d = min + (max - min) * r.nextFloat();  // -1 < d < 1

            long var = (long) (((float) pref / 10) * d * 3600000000000L);   // (pref/10) * random * (1hr)

            data.put("timestamp", (Long) data.get("timestamp") + var);

        }

        oos.writeObject(list);

        // If a peer is present
        Log.wtf(TAG, "Returning...");
        if(!peers.isEmpty()) {
            // Collaborate
            Log.wtf(TAG, peers.get(0).get("id"));
            return Integer.parseInt(peers.get(0).get("id"));
        }
        Log.wtf(TAG, "No peers");
        return 0;

    }

    /*
     * Collaborative stuff
     */
    /**/
    public void onPeersChanged (List<Map<String, String>> peers) {
        String TAG =  getClass().getName() + "@onPeersChanged: ";

        Log.wtf(TAG, "...");

        this.peers = peers;

        Log.wtf(TAG, peers.toString());

    }

    public boolean aggregateData (ObjectInputStream ois, ObjectOutputStream oos) throws Exception {
        String TAG =  getClass().getName() + "@aggregateData: ";

        Log.wtf(TAG, "...");

        // Read input stream
        List <Object> data = (List<Object>) ois.readObject();

        // Write to output stream
        oos.writeObject(data);

        return true;

    }
    /**/

}
