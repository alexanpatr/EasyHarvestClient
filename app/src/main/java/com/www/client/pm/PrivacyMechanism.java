package com.www.client.pm;

import com.www.client.Globals;
import com.www.client.Utils;

import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.*;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import dalvik.system.*;

public  class PrivacyMechanism implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static Context context;
    private static SharedPreferences sp;
    private static SharedPreferences.OnSharedPreferenceChangeListener spLsnr;

    private int stId, pmId, pmPreferences;

    private String pmClssName;
    private Class<Object> pmClss;
    private Object pmInst;
    private Method pmMthd;

    PmP2p pmP2p;

    public PrivacyMechanism (Context c, int pm, int st) {
        final String TAG = getClass().getName() + "@PrivacyMechanism: ";

        context = c;

        sp = PreferenceManager.getDefaultSharedPreferences(c);

        pmId = pm;
        stId = st;

        String pmDir = Globals.pms_dir + "/" + pmId;
        String stDir = Globals.client_dir + "/" + stId;

        /**/
        pmClssName = sp.getString(Globals.pm_clss, null);
        String pmZipUrl = pmDir + "/" + pmId + ".zip";
        File dex = c.getDir("dex", 0);

        DexClassLoader classLoader = new DexClassLoader(pmZipUrl, dex.getAbsolutePath(), null, this.getClass().getClassLoader());
        try {
            if (pmClss == null) {
                Log.wtf(TAG, "Instantiating PM " + pmId + " with class '" + pmClssName + "' for ST " + stId + " from " + pmZipUrl);
                pmClss = (Class<Object>) classLoader.loadClass(pmClssName);
                pmInst = pmClss.newInstance();
            } else {
                Log.wtf(TAG, "PM " + pmId + " with class '" + pmClssName + "' for ST " + stId + " already instantiated.");
            }
        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }
        /**/
    }

    /**
     * PrivacyMechanism functions
     */

    /**
     * void onStart (Context, int, ObjectInputStream)
     */
    public void onStart () {
        final String TAG = getClass().getName() + "@onStart: ";

        Log.wtf(TAG, "...");

        sp.registerOnSharedPreferenceChangeListener(this);

        pmPreferences = sp.getInt(Globals.privacy_level, 0);

        try {

            ObjectInputStream ois = Utils.getStateFrom(getId());

            // void onStart (Context, int, ObjectInputStream)
            pmMthd = pmClss.getMethod(
                    "onStart",
                    Context.class,
                    int.class,
                    ObjectInputStream.class);

            pmMthd.invoke(
                    pmInst,
                    context,
                    sp.getInt(Globals.privacy_level, 0),
                    ois);

            Utils.close(ois);

            // Collaborative staff
            /**/
            try {
                // void onPeersChanged (List<Map<String, String>>
                pmClss.getMethod("onPeersChanged",
                        List.class);

                pmP2p = new PmP2p(context, this);

            } catch (NoSuchMethodException e) {
                Log.wtf(TAG, "Could not find " + e.getMessage());
            }

            try {
                // boolean aggregateData (ObjectInputStream, ObjectOutputStream)
                pmClss.getMethod("aggregateData",
                        ObjectInputStream.class,
                        ObjectOutputStream.class);

                if(pmP2p == null) {
                    pmP2p = new PmP2p(context, this);
                }

            } catch (NoSuchMethodException e) {
                Log.wtf(TAG, "Could not find " + e.getMessage());
            }
            /**/

        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }
    }

    /**
     * void onStop ()
     */
    public void onStop () {
        final String TAG = getClass().getName() + "@onStop: ";

        Log.wtf(TAG, "...");

        if(pmP2p != null) {
            pmP2p.onStop();
            pmP2p = null;
        }

        /**/
        sp.unregisterOnSharedPreferenceChangeListener(this);

        // save state
        saveState();

        // stop
        try {
            // void onStop ()
            pmMthd = pmClss.getMethod(
                    "onStop");
            pmMthd.invoke(
                    pmInst);
        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }

        pmClss = null;
        /**/

    }

    /**
     * int processData (ObjectInputStream, ObjectOutputStream)
     * @param data
     */
    public void processData (List<Object> data) {
        String TAG = getClass().getName() + "@processData: ";

        Log.wtf(TAG, "...");

        /*
         * process data
         */
        int devId = 0;
        String dataPath = Utils.getNewDataPath(getStId());

        Log.wtf(TAG, "Saving data to " + dataPath);

        ObjectInputStream ois = Utils.getData(data);
        ObjectOutputStream oos = Utils.putData(dataPath);

        try {
            // int processData (ObjectInputStream, ObjectOutputStream)
            pmMthd = pmClss.getMethod(
                    "processData",
                    ObjectInputStream.class,
                    ObjectOutputStream.class);

            devId = (int) pmMthd.invoke(
                    pmInst,
                    ois,
                    oos);

            Utils.close(oos);
            Utils.close(ois);

        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }

        // saveState(getId());

        /**
         * Send to server or aggregator
         */
        Log.wtf(TAG, "devId == " + devId);
        if (devId == 0) {
            Log.wtf(TAG, "1");
            // Log.wtf(TAG, "Renaming data to " + new File(FilenameUtils.removeExtension(dataPath)).getPath());
            // new File(dataPath).renameTo(new File(FilenameUtils.removeExtension(dataPath)));
            Utils.saveData(Utils.getData(dataPath), String.valueOf(getStId()));
            new File(dataPath).delete();
        } else if (devId < 0) {
            Log.wtf(TAG, "2");
            Log.wtf(TAG, "Deleting data " + dataPath);
            new File(dataPath).delete();
        } else {
            Log.wtf(TAG, "3");

            String address = pmP2p.getPeerAddress(devId);
            if(pmP2p.isWifiP2pEnabled() && !address.isEmpty()) {
                Log.wtf(TAG, "Sending data to " + devId);
                Utils.mergeData(dataPath, Globals.pms_dir + "/" + pmId + "/" + devId + ".dat");
                pmP2p.sendToPeer(address);
            } else {
                Log.wtf(TAG, pmP2p.isWifiP2pEnabled() + " | " + address);
                Utils.saveData(Utils.getData(dataPath), String.valueOf(getStId()));
                new File(dataPath).delete();
            }

        }
    }

    /**
     * boolean saveState (ObjectOutputStream)
     * Called in:
     * - onStop
     * - processData
     * - aggregateData
     */
    private void saveState() {
        String TAG = getClass().getName() + "@saveState: ";

        Log.wtf(TAG, "...");

        String url = Globals.pms_dir + "/" + getId() + "/" + getId() + ".sav";

        Log.wtf(TAG, url);

        try {
            ObjectOutputStream oos = Utils.putStateTo(url);
            // boolean saveState (ObjectOutputStream)
            pmMthd = pmClss.getMethod(
                    "saveState",
                    ObjectOutputStream.class);

            boolean flag = (boolean) pmMthd.invoke(
                    pmInst,
                    oos);

            Utils.close(oos);

            if (flag) {
                Log.wtf(TAG, "OK");
            } else {
                Log.wtf(TAG, "No state saved");
                new File(url).delete();
            }

        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }

    }

    /**
     * void onPreferenceChanged (int)
     * Monitor if user changes the privacy level and inform the PM
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String TAG = getClass().getName() + "@onSharedPreferenceChanged: ";

        if (Globals.privacy_level.equals(key)) {
            Log.wtf(TAG, "new privacy level " + sp.getInt(Globals.privacy_level, 0));

            pmPreferences = sp.getInt(Globals.privacy_level, 0);
            /*
             * Call onPreferencesChanged
             */
            try {
//                void onPreferenceChanged (int)
                pmMthd = pmClss.getMethod(
                        "onPreferenceChanged",
                        int.class);

                pmMthd.invoke(
                        pmInst,
                        sp.getInt(Globals.privacy_level, 0));

            } catch (Exception e) {
                Log.wtf(TAG, "" + e.getMessage());
            }
        }
    }

    /**
     * void onPeersChanged (List<Map<String, String>>)
     * @param peers
     */
    void onPeersChanged (List<Map<String, String>> peers) {
        String TAG = getClass().getName() + "@onPeersChanged: ";

        Log.wtf(TAG, "...");

        List<Map<String, String>> list = new ArrayList<>();

        for (Map<String, String> p : peers) {
            Map<String, String> m = new HashMap<>();
            m.put("id", p.get("dev"));
            m.put("pref", p.get("pref"));
            list.add(m);
        }

        Log.wtf(TAG, list.toString());

        try {
            // void onPeersChanged (List<Map<String, String>>)
            pmMthd = pmClss.getMethod(
                    "onPeersChanged",
                    List.class);

            pmMthd.invoke(
                    pmInst,
                    list);

        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }

    }

    /**
     * boolean aggregateData (ObjectInputStream, ObjectOutputStream)
     * Called by PmP2p > connect > onConnectionInfoAvailable > PmServer
     * @param is
     */
    public void aggregateData(InputStream is) {
        String TAG = getClass().getName() + "@aggregateData: ";

        Log.wtf(TAG, "...");

        try {

            Log.wtf(TAG, "1");

            ObjectInputStream ois = new ObjectInputStream(is);

            Log.wtf(TAG, "2");

            String dataPath = Utils.getNewDataPath(getStId());

            Log.wtf(TAG, "3");

            ObjectOutputStream oos = Utils.putData(dataPath);

            Log.wtf(TAG, "4");

            boolean flag = false;

            try {
                // boolean aggregateData (ObjectInputStream, ObjectOutputStream)
                pmMthd = pmClss.getMethod(
                        "aggregateData",
                        ObjectInputStream.class,
                        ObjectOutputStream.class);

                Log.wtf(TAG, "5");

                flag = (boolean) pmMthd.invoke(
                        pmInst,
                        ois,
                        oos);

                Log.wtf(TAG, "6");

            } catch (Exception e) {
                Log.wtf(TAG, e.getMessage());
            }

            Log.wtf(TAG, "7");

            Utils.close(oos);
            Utils.close(ois);
            Utils.close(is);

            Log.wtf(TAG, "8");

            if (flag) {
                Utils.saveData(Utils.getData(dataPath), String.valueOf(getStId()));
            }

            Log.wtf(TAG, "9");

            new File(dataPath).delete();

            Log.wtf(TAG, "10");

            saveState();

            Log.wtf(TAG, "11");

        } catch (Exception e) {
            Log.wtf(TAG, e.getMessage());
        }

        Log.wtf(TAG, "OK");
    }

    /**
     * Misc.
     */

    public int getId () {
        final String TAG = getClass().getName() + "@getId: ";
        return pmId;
    }

    public int getVersion() {
        //TODO
        return 0;
    }

    public int getPreferences() {
        String TAG = getClass().getName() + "@getPreferences: ";
        return pmPreferences;
    }

    public int getStId () {
        String TAG = getClass().getName() + "@getStId: ";
        return stId;
    }

}
