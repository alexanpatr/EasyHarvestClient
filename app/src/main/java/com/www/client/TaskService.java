package com.www.client;

import android.app.*;
import android.content.*;
import android.location.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.widget.*;

import com.www.client.pm.PrivacyMechanism;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.json.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import dalvik.system.DexClassLoader;


public class TaskService extends Service implements LocationListener {

    private static Context context;

    private static final int API = android.os.Build.VERSION.SDK_INT;
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String CLIENT = SDCARD + "/Client";

    private static Class<Object> classToLoad = null;
    private static Object instance = null;
    private static Method method = null;

    private static boolean wifiConnected = false;
    private static boolean mobileConnected = false;
    public static boolean isConnected = false;
    public static boolean firstRun = true;

    public static boolean isWithinBorders = false;
    private static NetworkReceiver networkReceiver = null;
    private static LocationManager locationManager = null;

    private static ArrayList<JSONObject> privacyRegions = null;

    private static DeviceRegister deviceRegister = null;
    private static GetTaskInfo getTaskInfo = null;
    private static GetBin getBin = null;
    private static GetProp getProp = null;
    private static PutData putData = null;
    private static CheckData checkData = null;
    private static LogCat logCat = null;

    private static SharedPreferences sharedPref = null;
    private static SharedPreferences.Editor editor = null;

    private static PrivacyMechanism pm;

    /********** SERVICE FUNCTIONS **********/

    @Override
    public void onCreate() {
        String TAG = getClass().getName() + "@onStartCommand: ";
        super.onCreate();

        Log.wtf(TAG, "ON");
        TaskService.context = getApplicationContext();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String TAG = getClass().getName() + "@onStartCommand: ";

        Log.wtf(TAG, "Received start id " + startId + ": " + intent);
        if(intent != null && intent.getExtras() != null) {
            int request = intent.getExtras().getInt("request");
            Log.wtf(TAG, "request = " + request);
            switch (request) {
                case 0:
                    Log.wtf(TAG, "" + "Sleep...");
                    stopExe();
                    if(putData != null) {
                        putData.cancel(true);
                        putData.shutdownClient();
                    }
                    if(checkData != null) {
                        checkData.cancel(true);
                        checkData.shutdownClient();
                    }
                    unregisterLocationReceiver();
                    setTimeFrom(spGetString("timeFrom"), spGetString("timeTo"));
                    return START_STICKY;
                case 1:
                    Log.wtf(TAG, "" + "Wake up!");
                    //registerNetworkReceiver();
                    onStart();
                    return START_STICKY;
                default:
                    Log.wtf(TAG, "" + "default");
                    return START_STICKY;
            }
        }
        else {
            Log.wtf(TAG, "" + "else");
            registerNetworkReceiver();
            onStart();
            return START_STICKY;
        }
		/*/**/
    }

    @Override
    public void onDestroy() {
        String TAG = getClass().getName() + "@onDestroy: ";

        super.onDestroy();
        Log.wtf(TAG, "Shutting down...");

        // Disable BootReceiver
		/*ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);/**/

        onHalt();
        if(networkReceiver != null) unregisterNetworkReceiver();
        Log.wtf(TAG, "OFF");
    }

    public void onTest() {
        String TAG = getClass().getName() + "@onTest: ";
        Log.wtf(TAG, "...");
		/*
		editor.putString("deviceID", "ea3f8e1825fb4c3abb0904aa4dfbab3e");

		editor.putString("taskID", "1");
		editor.putString("className", "Demo");
		editor.putString("taskSize", "13130092");

		editor.putBoolean("timeStatus", true);
		editor.putString("timeFrom", "12:00");
		editor.putString("timeTo", "13:00");

		editor.putString("serviceStatus", "post");
		editor.putString("taskStatus", "start");
		//editor.putString("postStatus", "log");
		/**/

        editor.commit();

        //exeTask();
        //setTimeFrom("21:00", "23:00");
        //setTimeTo("20:00", "02:00");
        //Log.wtf(TAG, "onTest: " + "isWithinTime = " + isWithinTime("01:00", "21:00"));
    }

    public void onStart() {
        String TAG = getClass().getName() + "@onStart: ";

        Log.wtf(TAG, "Starting/Resuming...");
        //fileInit();
        onTest();
        if (spGetString("deviceID").isEmpty()) {
            deviceRegister();
        }
        else {
            if("get".equals(spGetString("serviceStatus"))) {
                if("getBin".equals(spGetString("getStatus"))){
                    getBin();
                }
                else {
                    getTaskInfo();
                }
            }
            else if("post".equals(spGetString("serviceStatus"))) {
                if("start".equals(spGetString("taskStatus"))) {
                    if(getProp != null) {
                        getProp.cancel(true);
                        getProp.shutdownClient();
                    }
                    getProp();
                    startExe();
                    if(!"log".equals(spGetString("postStatus"))) {
                        if("put".equals(spGetString("postStatus"))) {
                            //if(putData == null) {
                            putData();
                            //}
                        }
                        else {
                            //if(checkData == null) {
                            checkData();
                            //}
                        }
                    }
                }
                else if("pause".equals(spGetString("taskStatus"))) {
                    onPause();
                }
                else {
                    onStop();
                    onStart();
                }
            }
        }/**/
    }

    public void onHalt() {
        stopNetworking();
        stopExe();

        unregisterLocationReceiver();
    }

    public void onPause() {
        if(putData != null) {
            putData.cancel(true);
            putData.shutdownClient();
        }
        if(checkData != null) {
            checkData.cancel(true);
            checkData.shutdownClient();
        }
        unregisterLocationReceiver();
        cancelTime();
        stopExe();
        deletePendingData(spGetString("taskID"));
        getProp();
    }

    public void onStop() {
        stopNetworking();
        unregisterLocationReceiver();
        cancelTime();
        deletePendingData(spGetString("taskID"));
        stopExe();

//
//        Remove corresponding PM
        if (sharedPref.getString(Globals.pm_id, null) != null) {
            Utils.removePM(getApplicationContext(), Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")));
        }
//

        editor.putString("serviceStatus", "get");
        editor.putString("getStatus", "getTaskInfo");
        editor.putString("postStatus", "log");
        editor.commit();
        taskFin();

        onStart();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /********** SERVER FUNCTIONS **********/

    public void deviceRegister() {
        String TAG = getClass().getName() + "@deviceRegister: ";

        if(isOnline()) {
            Log.wtf(TAG, "Online");
            deviceRegister = new DeviceRegister();
            deviceRegister.addNameValuePair("username", sharedPref.getString("username", null));
            deviceRegister.addNameValuePair("model", android.os.Build.MODEL);
            deviceRegister.addNameValuePair("os", android.os.Build.VERSION.RELEASE);
            deviceRegister.execute(Globals.devs_url + "/register");
            Log.wtf(TAG, "Online");
        }
        else {
            Log.wtf(TAG, "Offline");
        }
    }

    public void getTaskInfo() {
        String TAG = getClass().getName() + "@getTaskInfo: ";

        if(isOnline()) {
            Log.wtf(TAG, "" + "Online.");
            getTaskInfo = new GetTaskInfo();
            getTaskInfo.execute();
        }
        else {
            Log.wtf(TAG, "getTaskInfo: " + "Offline.");
        }
    }

    public void getBin() {
        String TAG = getClass().getName() + "@getBin: ";

        if(isOnline()) {
            Log.wtf(TAG, "" + "Online.");
            String taskID = sharedPref.getString("taskID", null);
            File task = new File(CLIENT + "/" + taskID + "/" + taskID + ".zip");
            String url = "/" + taskID + "/getbin";
            if (task.exists()) url += "/" + task.length() + "/" + sharedPref.getString("deviceID", null);
            else url += "/" + sharedPref.getString("deviceID", null);
            Log.wtf(TAG, "" + url);
            getBin = new GetBin();
            getBin.execute(Globals.tasks_url + url);
        }
        else {
            Log.wtf(TAG, "" + "Offline.");
        }
    }

    private void getProp() {
        String TAG = getClass().getName() + "@getProp: ";

        if(isOnline()) {
            Log.wtf(TAG, "Online");
            String taskID = sharedPref.getString("taskID", null);
            String deviceID = sharedPref.getString("deviceID", null);
            String url = "/" + taskID + "/getprop/" + deviceID;
            Log.wtf(TAG, "" + url);
            getProp = new GetProp();
            getProp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Globals.tasks_url + url);
        }
        else {
            Log.wtf(TAG, "Offline");
        }
    }

    public void putData() {
        String TAG = getClass().getName() + "@putData: ";
        if(isOnline()) {
            Log.wtf(TAG, "" + "Online.");
            String taskID = sharedPref.getString("taskID", "");
            String deviceID = sharedPref.getString("deviceID", "");
            File data = Utils.returnFileFrom(CLIENT + "/" + taskID, ".dat");
            if(data != null) {
                Log.wtf(TAG, "" + "Found data " + data);

//                String url = "/" + taskID + "/putdata/" + taskID + "/" + deviceID;

                String dataName = data.getName().substring(0, data.getName().indexOf("."));
                editor.putString("dataName", dataName);
                editor.commit();

                String url = "/" + taskID + "/putdata/" + dataName + "/" + deviceID;

                Log.wtf(TAG, "" + url);

                Log.wtf(TAG, "1/3");
                putData = new PutData();
                Log.wtf(TAG, "2/3");
                putData.execute(Globals.tasks_url + url);
                Log.wtf(TAG, "3/3");
            }
            else {
                Log.wtf(TAG, "" + "No new data found.");

                editor.putString("postStatus", "log");
                editor.commit();
            }
        }
        else {
            Log.wtf(TAG, "" + "Offline.");
        }
    }

    public void checkData() {
        String TAG = getClass().getName() + "@checkData: ";

        if(isOnline()) {
            Log.wtf(TAG, "" + "Online.");
            String taskID = sharedPref.getString("taskID", null);
            String deviceID = sharedPref.getString("deviceID", null);

//            File data = new File(CLIENT + "/" + taskID + "/" + taskID + ".dat");

            String dataName = sharedPref.getString("dataName", null);
            File data = new File(CLIENT + "/" + taskID + "/" + dataName + ".dat");

            if(data.exists()) {

//                String url = "/" + taskID + "/checkdata/" + taskID + "_" + data.length() + "/" + deviceID;

                String url = "/" + taskID + "/checkdata/" + dataName + "_" + data.length() + "/" + deviceID;

                Log.wtf(TAG, "" + url);
                checkData = new CheckData();
                checkData.execute(Globals.tasks_url + url);
            }
            else {
                Log.wtf(TAG, "" + "Data " + dataName + " not found.");
            }
        }
        else {
            Log.wtf(TAG, "" + "Offline.");
        }
    }

    private void startExe() {
        String TAG = getClass().getName() + "@startExe: ";

        Log.wtf(TAG, "" + "...");
        if (classToLoad == null) {
            if((sharedPref.getBoolean("locStatus", false) || sharedPref.getBoolean("privacyRegionsStatus", false))
                    && sharedPref.getBoolean("timeStatus", false)) {
                Log.wtf(TAG, "" + "Time: ON");
                Log.wtf(TAG, "" + "Location: ON");
                registerLocationReceiver();
                if(isWithinBorders() && isWithinTime(spGetString("timeFrom"), spGetString("timeTo"))) {
                    Log.wtf(TAG, "" + "Within time.");
                    Log.wtf(TAG, "" + "Within borders.");
                    setTimeTo(spGetString("timeFrom"), spGetString("timeTo"));
                    exeTask();
                }
                else if(isWithinBorders() && !isWithinTime(spGetString("timeFrom"), spGetString("timeTo"))) {
                    Log.wtf(TAG, "" + "Out of time.");
                    Log.wtf(TAG, "" + "Within borders.");
                    setTimeFrom(spGetString("timeFrom"), spGetString("timeTo"));
                    unregisterLocationReceiver();
                }
                else if(!isWithinBorders() && isWithinTime(spGetString("timeFrom"), spGetString("timeTo"))) {
                    Log.wtf(TAG, "" + "Within time.");
                    Log.wtf(TAG, "" + "Out of borders.");
                    setTimeTo(spGetString("timeFrom"), spGetString("timeTo"));
                }
                else {
                    Log.wtf(TAG, "" + "Out of time.");
                    Log.wtf(TAG, "" + "Out of borders.");
                    setTimeFrom(spGetString("timeFrom"), spGetString("timeTo"));
                    unregisterLocationReceiver();
                }
            }
            else if((sharedPref.getBoolean("locStatus", false) || sharedPref.getBoolean("privacyRegionsStatus", false))
                    && !sharedPref.getBoolean("timeStatus", false)) {
                if(sharedPref.getBoolean("locStatus", false)) {
                    Log.wtf(TAG, "" + "Time: OFF");
                    Log.wtf(TAG, "" + "Location: ON");
                    registerLocationReceiver();
                    if(isWithinBorders()) {
                        Log.wtf(TAG, "" + "Within borders.");
                        exeTask();
                    }
                    else {
                        Log.wtf(TAG, "" + "Out of borders.");
                    }
                }
            }
            else if((!sharedPref.getBoolean("locStatus", false) && !sharedPref.getBoolean("privacyRegionsStatus", false))
                    && sharedPref.getBoolean("timeStatus", false)) {
                Log.wtf(TAG, "" + "Time: ON");
                Log.wtf(TAG, "" + "Location: OFF");
                if(isWithinTime(spGetString("timeFrom"), spGetString("timeTo"))) {
                    Log.wtf(TAG, "" + "Within time.");
                    setTimeTo(spGetString("timeFrom"), spGetString("timeTo"));
                    exeTask();
                }
                else {
                    Log.wtf(TAG, "" + "Out of time.");
                    setTimeFrom(spGetString("timeFrom"), spGetString("timeTo"));
                }
            }
            else {
                Log.wtf(TAG, "" + "Time: OFF");
                Log.wtf(TAG, "" + "Location: OFF");
                exeTask();
            }
        }
        else {
            Log.wtf(TAG, "" + "Already executing.");
        }
    }

    private void stopExe() {
        String TAG = getClass().getName() + "@stopExe: ";

        Log.wtf(TAG, "" + "...");

        // Toggled in ClientActivity@onTest()
        if (sharedPref.getBoolean(Globals.easy_privacy, false)) {
            if (pm != null) {
                Log.wtf(TAG, "Stopping PM " + "...");

                pm.onStop();
                pm = null;

                Log.wtf(TAG, "Stopping PM " + "OK");
            }
        }

        Log.wtf(TAG, "Stopping ST " + "...");
        if(logCat != null) logCat.cancel(true);
        if(classToLoad != null) {
            try {
                String taskID = sharedPref.getString("taskID", "");
                method = classToLoad.getMethod("saveState", ObjectOutputStream.class);
                ObjectOutputStream os = saveState(taskID);
                if (os != null) {
                    if (!(Boolean) method.invoke(instance, saveState(taskID))) {
                        close(os);
                        new File(CLIENT + "/" + taskID + "/" + taskID + ".sav").delete();
                    } else {
                        close(os);
                    }
                }
                classToLoad.getMethod("onStop").invoke(instance);
            } catch (Exception e) {
                Log.wtf(TAG, e.getMessage());
            }
        }
        classToLoad = null;
        Log.wtf(TAG, "Stopping ST " + "OK");
    }

    private void exeTask() {
        String TAG = getClass().getName() + "@exeTask: ";

        String taskID = sharedPref.getString("taskID", "");
        String className = sharedPref.getString("className", "");
        Log.wtf(TAG, "" + "...");
        if(classToLoad == null) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            if(new File(CLIENT + "/" + taskID + "/" + taskID + ".sav").exists()) {
                Log.wtf(TAG, "" + "Found saved state.");
                try {
                    fis = new FileInputStream(CLIENT + "/" + taskID + "/" + taskID + ".sav");
                    ois = new ObjectInputStream(fis);
                    //savedInstance = (Object) ois.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Log.wtf(TAG, "" + "Loading...");
                String url = CLIENT + "/" + taskID + "/" + taskID + ".zip";
                File dir = getDir("dex", 0);
                DexClassLoader classLoader = new DexClassLoader(url, dir.getAbsolutePath(), null, this.getClass().getClassLoader());
                classToLoad = (Class<Object>) classLoader.loadClass(className);
                instance = classToLoad.newInstance();
                method = classToLoad.getMethod("onStart", Context.class, ObjectInputStream.class);
                method.invoke(instance, getApplicationContext(), ois);

                close(ois);
                close(fis);
                //oos.close();
                //fos.close();
                Log.wtf(TAG, "" + taskID + "|" + className + " loaded successfully.");



            } catch (Exception e) {
                e.printStackTrace();
            }
            logCat();
        }
        else Log.wtf(TAG, "" + "Already executing.");
    }

    private void logCat() {
        String TAG = getClass().getName() + "@logCat: ";

        Log.wtf(TAG, "...");
        logCat = new LogCat();
        logCat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**********  **********/

    private class DeviceRegister extends AsyncTask<String, Void, String> {
        private static final String TAG = "DeviceRegister";
        private HttpClient client;
        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        public void addNameValuePair(String name, String value) {
            params.add(new BasicNameValuePair(name, value));
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.wtf(TAG, "RegisterDeviceTask: doInBackground...");
            String result = "oops";
            client = new DefaultHttpClient();
            HttpResponse response = null;
            try {
                HttpPost request = new HttpPost(urls[0]);
                request.setEntity(new UrlEncodedFormEntity(params));
                response = client.execute(request);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if(response != null) {
                try {
                    result = Utils.writeToString(response.getEntity().getContent());
                    editor.putString("deviceID", result);
                    editor.putString("serviceStatus", "get");
                    editor.putString("getStatus", "getTaskInfo");
                    editor.commit();
                    result = "device received id " + result;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            else result = "connection error";
            return result;
        }

        protected void onPostExecute(String result) {
            Log.wtf(TAG, "onPostExecute: " + result);
            if(!isCancelled()) onStart();
        }

        protected void onCancelled(String result) {
            Log.wtf(TAG, "onCancelled: " + "...");
        }

        public void shutdownClient () {
            if (client != null) client.getConnectionManager().shutdown();
        }
    }

    private class GetTaskInfo extends AsyncTask<Void, Void, String> {

        private static final String TAG = "GetTaskInfo";
        private String deviceID = spGetString("deviceID");
        private HttpClient client;
        private JSONObject task = null;

        @Override
        protected String doInBackground(Void... arg0) {
            Log.wtf(TAG, "doInBackground: " + "...");
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(Globals.tasks_url + "/gettaskinfo/" + deviceID);
            HttpResponse response = null;
            String result = "oops";
            while (!isCancelled() && "oops".equals(result) && isOnline()) {
                try {
                    response = client.execute(request);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if(response != null) {
                    try {
                        task = new JSONObject(Utils.writeToString(response.getEntity().getContent()));
                        if (!taskIsNull(task)) result = task.toString();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    if ("oops".equals(result)) {
                        try {
                            int div = sharedPref.getInt("serviceLevel", 1);
                            if (div == 0) div = 1;
                            Thread.sleep(Globals.task_latency/div);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                }
                else result = "Connection error";
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.wtf(TAG, "onPostExecute: " + result + ".");
            if (task != null) {
                try {
                    TaskService.editor.putString("serviceStatus", "get");
                    TaskService.editor.putString("getStatus", "getBin");
                    TaskService.editor.putString("taskID", task.getString("taskID"));
                    TaskService.editor.putString("taskSize", task.getString("taskSize"));
                    TaskService.editor.putString("taskStatus", task.getString("taskStatus"));
                    TaskService.editor.putString("className", task.getString("className"));
                    TaskService.editor.putString("dataName", "");
                    TaskService.editor.putString("dataChecked", "0");
                    TaskService.editor.putBoolean("locStatus", false);
                    if(("on").equals(task.getString("locStatus"))) {
                        TaskService.editor.putBoolean("locStatus", true);
                        TaskService.editor.putString("locSWlat", task.getString("locSWlat"));
                        TaskService.editor.putString("locSWlng", task.getString("locSWlng"));
                        TaskService.editor.putString("locNElat", task.getString("locNElat"));
                        TaskService.editor.putString("locNElng", task.getString("locNElng"));
                    }
                    TaskService.editor.putBoolean("timeStatus", false);
                    if(("on").equals(task.getString("timeStatus"))) {
                        TaskService.editor.putBoolean("timeStatus", true);
                        TaskService.editor.putString("timeFrom", task.getString("timeFrom"));
                        TaskService.editor.putString("timeTo", task.getString("timeTo"));
                    }
                    TaskService.editor.commit();
                    new File(TaskService.CLIENT + "/" + task.getString("taskID")).mkdir();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!isCancelled()) getBin();
            }
            else {
                if(!isCancelled()) onStart();
            }
        }

        protected void onCancelled(String result) {
            Log.wtf(TAG, "onCancelled: " + "...");
        }

        public void shutdownClient () {
            if (client != null) client.getConnectionManager().shutdown();
        }
    }

    private class GetBin extends AsyncTask<String, Void, String> {
        private static final String TAG = "GetBin";
        private HttpClient client;

        @Override
        protected String doInBackground(String... urls) {
            Log.wtf(TAG, "doInBackground: " + "...");
            String result = "oops";
            String taskID = sharedPref.getString("taskID", null);
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            HttpResponse response = null;
            try {
                response = client.execute(request);
                if(response != null) {
                    String taskUrl = CLIENT + "/" + taskID + "/" + taskID + ".zip";
                    Utils.writeToFile(response.getEntity().getContent(), taskUrl);
                    result = "Bin received";
                }
                else {
                    result = "Connection error";
                }
            } catch (Exception e) {}
            return result;
        }

        protected void onCancelled(String result) {
            Log.wtf(TAG, "onCancelled: " + "...");
            checkBin();
        }

        protected void onPostExecute(String result) {
            Log.wtf(TAG, "onPostExecute: " + result + ".");
            checkBin();
            if(!isCancelled()) onStart();
        }

        private void checkBin() {
            String taskID = sharedPref.getString("taskID", null);
            String taskUrl = CLIENT + "/" + taskID + "/" + taskID + ".zip";
            if (new File(taskUrl).length() == Long.valueOf(sharedPref.getString("taskSize", null))) {
                Log.wtf(TAG, "checkBin: " + "OK" + ".");
                TaskService.editor.putString("serviceStatus", "post");
                TaskService.editor.putString("postStatus", "log");
                TaskService.editor.commit();
            }
            else if (new File(taskUrl).length() < Long.valueOf(sharedPref.getString("taskSize", null))) {
                Log.wtf(TAG, "checkBin: " + new File(taskUrl).length() + "/" + spGetString("taskSize") + ".");
            }
            else {
                onStop();
                onStart();
            }
        }

        public void shutdownClient () {
            if (client != null) client.getConnectionManager().shutdown();
        }
    }

    private class GetProp extends AsyncTask<String, Void, String> {

        private static final String TAG = "GetProp";
        private HttpClient client;
        private JSONObject prop = null;

        @Override
        protected String doInBackground(String... urls) {
            Log.wtf(TAG, "doInBackground: " + "...");
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            HttpResponse response = null;
            String result = "oops";
            String status = "";
            Boolean locationChanged = false;
            Boolean timeChanged = false;
            while(!isCancelled() && isOnline()) {
                try {
                    response = client.execute(request);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if(response != null) {
                    try {
                        prop = new JSONObject(Utils.writeToString(response.getEntity().getContent()));
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    if (prop != null && !taskIsNull(prop)) {
                        try {
                            if(TaskService.sharedPref.getBoolean("locStatus", false)) {
                                locationChanged = locationChanged(prop, TaskService.sharedPref);
                                if(locationChanged) {
                                    Log.wtf(TAG, "doInBackground: " + "Location changed.");
                                    TaskService.editor.putString("locSWlat", prop.getString("locSWlat"));
                                    TaskService.editor.putString("locSWlng", prop.getString("locSWlng"));
                                    TaskService.editor.putString("locNElat", prop.getString("locNElat"));
                                    TaskService.editor.putString("locNElng", prop.getString("locNElng"));
                                    TaskService.editor.commit();
                                }
                            }
                            if(TaskService.sharedPref.getBoolean("timeStatus", false)) {
                                timeChanged = timeChanged(prop, TaskService.sharedPref);
                                if(timeChanged) {
                                    Log.wtf(TAG, "doInBackground: " + "Time changed.");
                                    TaskService.editor.putString("timeFrom", prop.getString("timeFrom"));
                                    TaskService.editor.putString("timeTo", prop.getString("timeTo"));
                                    TaskService.editor.commit();
                                }
                            }
                            if(!prop.isNull("taskStatus") && !prop.getString("taskStatus").equals(spGetString("taskStatus"))) {
                                TaskService.editor.putString("taskStatus", prop.getString("taskStatus"));
                                TaskService.editor.commit();
                                return prop.getString("taskStatus");
								/**/
                            }
                            else {
                                if(timeChanged) onTimeChanged();
                                if(locationChanged) {
                                    if (locationManager != null) {
                                        onLocationChanged(getCurrentLocation());
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        int div = sharedPref.getInt("serviceLevel", 1);
                        if (div == 0) div = 1;
                        Thread.sleep(Globals.task_latency/div);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
                else {
                    result = "Connection error";
                }
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.wtf(TAG, "onPostExecute: " + result + ".");
            if("start".equals(result)) {
                onStart();
            }
            else if("pause".equals(result)) {
                onPause();
            }
            else if("stop".equals(result)) {
                onStop();
            }
        }

        public void shutdownClient () {
            if (client != null) client.getConnectionManager().shutdown();
        }

        protected void onCancelled(String result) {
            Log.wtf(TAG, "onCancelled: " + "...");
        }

    }

    private class PutData extends AsyncTask<String, Void, String> {

        private HttpURLConnection connection = null;

        @Override
        protected String doInBackground(String... params) {
            String TAG = getClass().getName() + "@doInBackground: ";

            Log.wtf(TAG, "1");

            String taskID = TaskService.sharedPref.getString("taskID", null);

            Log.wtf(TAG, "2 " + taskID);

            String dataName = TaskService.sharedPref.getString("dataName", null);

            Log.wtf(TAG, "3 " + dataName);

            final String dataChecked = TaskService.sharedPref.getString("dataChecked", "0");

            Log.wtf(TAG, "4 " + dataChecked);

//            final String dataUrl = TaskService.CLIENT + "/" + taskID + "/" + taskID + ".dat";

            String dataUrl = TaskService.CLIENT + "/" + taskID + "/" + dataName + ".dat";

            String result = "oops";

            Log.wtf(TAG, "5 " + dataUrl);

            try {
                Log.wtf(TAG, "Sending " + taskID + ".dat from " + dataChecked + "...");
                TaskService.editor.putString("postStatus", "check");
                TaskService.editor.commit();
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty("Connection", "close");
                Utils.returnPart(new RandomAccessFile(new File(dataUrl), "r"), connection.getOutputStream(), Long.valueOf(dataChecked));
                InputStream response = new BufferedInputStream(connection.getInputStream());
                result = Utils.writeToString(response);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            finally {
                if(connection != null) {
                    connection.disconnect();
                    connection = null;
                    return result;
                }
            }
            return result;
        }

        public void shutdownClient () {
            if (connection != null) connection.disconnect();
        }

        protected void onCancelled(String result) {
            String TAG = getClass().getName() + "@onCancelled: ";

            Log.wtf(TAG, "" + "...");
        }

        protected void onPostExecute(String result) {
            String TAG = getClass().getName() + "@onPostExecute: ";

            Log.wtf(TAG, "" + result + ".");
            checkData();

        }
    }

    private class CheckData extends AsyncTask<String, Void, Void> {
        private HttpClient client;
        private JSONObject data = null;

        String taskID = TaskService.sharedPref.getString("taskID", null);
        String deviceID = TaskService.sharedPref.getString("deviceID", null);
        String dataName = TaskService.sharedPref.getString("dataName", null);

        @Override
        protected Void doInBackground(String... urls) {
            String TAG = getClass().getName() + "@doInBackground: ";

            Log.wtf(TAG, "...");
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            HttpResponse HttpResponse = null;
            String result = "oops";
            String response = "";
            String checked = "0";
            try {
                HttpResponse = client.execute(request);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if(HttpResponse != null) {
                try {
                    data = new JSONObject(Utils.writeToString(HttpResponse.getEntity().getContent()).replace("[", "").replace("]", "").replace("\"", ""));
                    result = data.toString();
                    response = data.getString("response");
                    if ("put".equals(response)) {
                        checked = data.getString("checked");
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                Log.wtf(TAG, "" + result);
                String dir = CLIENT + "/" + taskID;
                if("OK".equals(response)) {
                    TaskService.editor.putString("dataChecked", "0");
                    TaskService.editor.commit();

                    //new File(dir + "/" + dataName + ".dat.new").renameTo(new File(dir + "/" + dataName + ".dat"));
                    //mergeData(taskID, dataName);

//                    new File(dir + "/" + taskID + ".dat").delete();

                    new File(dir + "/" + dataName + ".dat").delete();

                    TaskService.editor.putString("postStatus", "log");
                    TaskService.editor.commit();

					File data = Utils.returnFileFrom(dir, ".dat");

                    if(data != null) {
                        TaskService.editor.putString("postStatus", "put");
                        TaskService.editor.commit();
                        if(!isCancelled()) {
                            Log.wtf(TAG, "Recalling putData!");
                            putData();
                        }
                    }
                    else {
                        TaskService.editor.putString("postStatus", "log");
                        TaskService.editor.commit();
                    }
					/**/
                }
                else if("oops".equals(response)) {
                    TaskService.editor.putString("dataChecked", "0");
                    TaskService.editor.putString("postStatus", "put");
                    TaskService.editor.commit();
                    if(!isCancelled()) putData();
                }
                else if("put".equals(response)){
                    TaskService.editor.putString("dataChecked", checked);
                    TaskService.editor.putString("postStatus", "put");
                    TaskService.editor.commit();
                    if(!isCancelled()) putData();
                }
                else {}
            }
            else Log.wtf(TAG, "" + "Connection error.");
            return null;
        }

        public void shutdownClient () {
            if (client != null) client.getConnectionManager().shutdown();
        }

        protected void onCancelled(String result) {
            String TAG = getClass().getName() + "@onCancelled: ";
            Log.wtf(TAG, "" + "...");
        }
    }

    private class LogCat extends AsyncTask<String, Void, Void> {

        String taskID = sharedPref.getString("taskID", "");

        @Override
        protected Void doInBackground(String... arg0) {
            String TAG = getClass().getName() + "@doInBackground: ";

            Log.wtf(TAG, "...");

            while(!isCancelled()) {
                Log.wtf(TAG, "POST == " + sharedPref.getString("postStatus", "null"));
                /*
                 * Check for new data
                 */
                List<Object> data = new ArrayList<>();
                try {
                    data = (List<Object>) classToLoad.getMethod("getData").invoke(instance);

                } catch (Exception e) {
                    Log.wtf(TAG, e.getMessage());
                }

//                Check data integrity
                if (data != null && !data.isEmpty() && checkDataKeys(data) && checkDataValues(data)) {
//                    Add extra information
                    Log.wtf(TAG, "New data.");
                    for (Object o : data) {
                        ((Map) o).put("device", sharedPref.getString("deviceID", "0"));
                        ((Map) o).put("task", sharedPref.getString("taskID", "0"));
                    }

//                    EasyPrivacy staff
//                    EP status toggled in ClientActivity@onTest()
                    if (sharedPref.getBoolean(Globals.easy_privacy, false)
                            && sharedPref.getString(Globals.pm_id, null) != null) {

//                        Detected installed PM

                        if (pm == null) {

//                            PM is inactive

                            if(sharedPref.getString(Globals.pm_st_id, null) != null &&
                                    sharedPref.getString(Globals.st_id, null) != null &&
                                    sharedPref.getString(Globals.pm_st_id, "").equals(sharedPref.getString(Globals.st_id, ""))) {

//                                PM is compatible with ST

//                                Instantiate PM
                                Log.wtf(TAG, "Starting PM " + sharedPref.getString(Globals.pm_id, null) + "...");
                                pm = new PrivacyMechanism(
                                        getApplicationContext(),
                                        Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")),
                                        Integer.valueOf(taskID));

//                                Start PM
                                pm.onStart();

//                                Pass data to PM
                                Log.wtf(TAG, "Passing data to PM...");
                                pm.processData(data);

                            } else {

//                                PM is incompatible with ST

                                Log.wtf(TAG, "Removing PM " + sharedPref.getString(Globals.pm_id, null) + "...");

//                                Remove PM
                                Utils.removePM(getApplicationContext(), Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")));

                                Log.wtf(TAG, "Removed PM " + sharedPref.getString(Globals.pm_id, "*") + sharedPref.getString(Globals.pm_name, "") + "...");

//                                Save data to file
                                Log.wtf(TAG, "Saving data...");
                                Utils.saveData(data, taskID);

                            }
                        } else {

//                            PM is active

                            if(!sharedPref.getString(Globals.st_id, "").equals(String.valueOf(pm.getStId()))) {

//                                PM is incompatible with ST

                                Log.wtf(TAG, "Halting and removing active PM " + sharedPref.getString(Globals.pm_id, null) + "...");

//                                Stop
                                pm.onStop();
                                pm = null;

//                                Remove
                                Utils.removePM(getApplicationContext(), Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")));

                            } else if (String.valueOf(pm.getId()).equals(sharedPref.getString(Globals.pm_id, null))) {

//                                PM is compatible with ST

//                                Pass data to PM
                                Log.wtf(TAG, "Passing data to PM...");
                                pm.processData(data);

                            } else {

//                                PM changed, restart PM

                                Log.wtf(TAG, "Stopping PM " + pm.getId() + "...");

//                                Stop old PM
                                pm.onStop();

                                Log.wtf(TAG, "Starting PM " + sharedPref.getString(Globals.pm_id, null) + "...");

//                                Instantiate new PM
                                pm = new PrivacyMechanism(
                                        getApplicationContext(),
                                        Integer.valueOf(sharedPref.getString(Globals.pm_id, "0")),
                                        Integer.valueOf(taskID));

//                                Start new PM
                                pm.onStart();

//                                Pass data to PM
                                Log.wtf(TAG, "Passing data to PM...");
                                pm.processData(data);

                            }
                        }
                    } else {
//                        Save data to file
                        Log.wtf(TAG, "Saving data...");
                        Utils.saveData(data, taskID);
                    }


                    /*
                     * Save Sensing Task state
                     */
                    try {
                        ObjectOutputStream oos = saveState(taskID);

                        method = classToLoad.getMethod("saveState", ObjectOutputStream.class);
                        boolean flag = (boolean) method.invoke(instance, oos);

                        close(oos);

                        if (!flag) {
                            new File(CLIENT + "/" + taskID + "/" + taskID + ".sav").delete();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*
                     * send data to Server
                     */
                    if ("log".equals(sharedPref.getString("postStatus", "log"))) {
//                    Log.wtf(TAG, "postStatus = put");

                        editor.putString("postStatus", "put");
                        editor.commit();

                        if (!isCancelled()) {
//                        Log.wtf(TAG, "put data");
                            putData();
                        }
                    }
                } else if(returnFileFrom(Globals.client_dir + "/" + taskID, ".dat") != null) {
                    Log.wtf(TAG, "Found data file @ " + taskID);
                    if ("log".equals(sharedPref.getString("postStatus", "log"))) {
//                    Log.wtf(TAG, "postStatus = put");

                        editor.putString("postStatus", "put");
                        editor.commit();

                        if (!isCancelled()) {
//                        Log.wtf(TAG, "put data");
                            putData();
                        }
                    }
                }

                /*
                 * Wait
                 */
                try {
                    int div = sharedPref.getInt("serviceLevel", 1);
                    if (div == 0) {
                        div = 1;
                    }
                    Thread.sleep(Globals.task_latency/div);
                } catch (InterruptedException e) {
                    Log.wtf(TAG, e.getMessage());
                }


				/*
                ArrayList<String> data = new ArrayList<String>();
                try {
                    data = (ArrayList<String>)classToLoad.getMethod("getData").invoke(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if(!data.isEmpty()) {
                    Log.wtf(TAG, "doInBackground: " + "New data.");
                    try {
//						for(Object object : data) {
//							writeObjectToFile(object, taskID);
//						}
                        saveData(data, taskID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        method = classToLoad.getMethod("saveState", new Class[] { ObjectOutputStream.class });
                        if (!(Boolean) method.invoke(instance, new Object[] { saveState(taskID) })) {
                            new File(CLIENT + "/" + taskID + "/" + taskID + ".sav").delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if("log".equals(sharedPref.getString("postStatus", "log"))) {
                        editor.putString("postStatus", "put");
                        editor.commit();
                        if(!isCancelled()) putData();
                    }
                }
                try {
                    int div = sharedPref.getInt("serviceLevel", 1);
                    if (div == 0) div = 1;
                    Thread.sleep(Globals.task_latency/div);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
                */

            }
            return null;
        }
    }

    /********** NETWORKING **********/

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if(activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        else {
            wifiConnected = false;
            mobileConnected = false;
        }
        if(((sharedPref.getString("listPref", "Wi-Fi").equals(ANY)) && (wifiConnected || mobileConnected)) || ((sharedPref.getString("listPref", "Wi-Fi").equals(WIFI)) && (wifiConnected))) {
            isConnected = true;
            return true;
        }
        else {
            isConnected = false;
            return false;
        }
    }

    private void stopNetworking() {
        String TAG = getClass().getName() + "@stopNetworking: ";

        Log.wtf(TAG, "" + "...");
        if(deviceRegister != null) {
            deviceRegister.cancel(true);
            deviceRegister.shutdownClient();
        }
        if(getTaskInfo != null) {
            getTaskInfo.cancel(true);
            getTaskInfo.shutdownClient();
        }
        if(getBin != null) {
            getBin.cancel(true);
            getBin.shutdownClient();
        }
        if(getProp != null) {
            getProp.cancel(true);
            getProp.shutdownClient();
        }
        if(putData != null) {
            putData.cancel(true);
            putData.shutdownClient();
        }
        if(checkData != null) {
            checkData.cancel(true);
            checkData.shutdownClient();
        }
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = getClass().getName() + "@onReceive: ";

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if ((WIFI.equals(sharedPref.getString("listPref", "Wi-Fi")) && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) ||
                    (ANY.equals(sharedPref.getString("listPref", "Wi-Fi")) && networkInfo != null)) {
                if(!isConnected) {
                    Log.wtf(TAG, "" + "Connected.");
                    isConnected = true;
                    //if(!firstRun) {
                    //onHalt();
                    onStart();
                    //}
                    firstRun = false;
                }
            }
            else {
                Log.wtf(TAG, "" + "Disconnected.");
                isConnected = false;
                //stopNetworking();
            }
        }
    }

    public void registerNetworkReceiver() {
        String TAG = getClass().getName() + "@registerNetworkReceiver: ";

        Log.wtf(TAG, "" + "...");
        if (networkReceiver == null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            networkReceiver = new NetworkReceiver();
            this.registerReceiver(networkReceiver, filter);
        }
        else {
            Log.wtf(TAG, "" + "Already registered.");
        }
    }

    public void unregisterNetworkReceiver() {
        String TAG = getClass().getName() + "@unregisterNetworkReceiver: ";

        Log.wtf(TAG, "" + "...");
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
        else {
            Log.wtf(TAG, "" + "Already unregistered.");
        }
    }

    /********** LOCATION **********/

    private Location getCurrentLocation() {
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if(provider == null) provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        return location;
    }

    private double getCoordinate(String string) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return Double.parseDouble(sp.getString(string, "0"));
    }

    private boolean isWithinBorders() {
        //Log.wtf(TAG, "isWithinBorders: " + "...");
        Boolean result = false;
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if(provider == null) provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null) {
            result = isWithinBorders(location);
        }
        return result;
    }

    private boolean isWithinBorders(Location loc) {
        boolean result = false;
        if (sharedPref.getBoolean("locStatus", false)) {
            if (Double.compare(loc.getLatitude(), getCoordinate("locSWlat")) >= 0 &&
                    Double.compare(loc.getLatitude(), getCoordinate("locNElat")) <= 0 &&
                    Double.compare(loc.getLongitude(), getCoordinate("locSWlng")) >= 0 &&
                    Double.compare(loc.getLongitude(), getCoordinate("locNElng")) <= 0) {
                result = true;
            } else return false;
        }
        if(sharedPref.getBoolean("privacyRegionsStatus", false)) {
            ArrayList<JSONObject> privacyRegions = Utils.fileToJSON(new File(CLIENT + "/" + "Settings" + "/" + "PrivacyRegions"));
            for (JSONObject privacyRegion : privacyRegions) {
                try {
                    if (Double.compare(loc.getLatitude(), privacyRegion.getDouble("locSWlat")) >= 0 &&
                            Double.compare(loc.getLatitude(), privacyRegion.getDouble("locNElat")) <= 0 &&
                            Double.compare(loc.getLongitude(), privacyRegion.getDouble("locSWlng")) >= 0 &&
                            Double.compare(loc.getLongitude(), privacyRegion.getDouble("locNElng")) <= 0) {
                        return false;
                    } else result = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private boolean locationChanged(JSONObject prop, SharedPreferences pref) {
        try {
            if (!prop.isNull("locSWlat") && !prop.isNull("locSWlng") && !prop.isNull("locNElat") && !prop.isNull("locNElng")) {
                if (!prop.getString("locSWlat").equals(pref.getString("locSWlat", "0"))) {
                    return true;
                }
                else if (!prop.getString("locSWlng").equals(pref.getString("locSWlng", "0"))) {
                    return true;
                }
                else if (!prop.getString("locNElat").equals(pref.getString("locNElat", "0"))) {
                    return true;
                }
                else if (!prop.getString("locNElng").equals(pref.getString("locNElng", "0"))) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        String TAG = getClass().getName() + "@onLocationChanged: ";

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.wtf(TAG, "" + "New location.\nLat: " + latitude + "\nLng: " + longitude);
        if(isWithinBorders(location)) {
            Log.wtf(TAG, "" + "Inside borders.");
            startExe();
        }
        else {
            Log.wtf(TAG, "" + "Outside borders.");
            stopExe();
        }/**/
    }

    @Override
    public void onProviderDisabled(String provider) {
        String TAG = getClass().getName() + "@onProviderDisabled: ";

        Log.wtf(TAG, "" + provider);
        if(isWithinBorders()) {
            Log.wtf(TAG, "" + "Within borders.");
            //exe();
        }
        else {
            Log.wtf(TAG, "" + "Outside borders.");
            //stopExe();
        }/**/
    }

    @Override
    public void onProviderEnabled(String provider) {
        String TAG = getClass().getName() + "@onProviderEnabled: ";

        Log.wtf(TAG, "" + provider);
        if(isWithinBorders()) {
            Log.wtf(TAG, "" + "Within borders.");
            //exeTask();
        }
        else {
            Log.wtf(TAG, "" + "Outside borders.");
            //stopExe();
        }/**/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String TAG = getClass().getName() + "@onStatusChanged: ";

        Log.wtf(TAG, "" + provider);
        Log.wtf(TAG, "" + status);
        Log.wtf(TAG, "" + extras);
    }

    public void registerLocationReceiver() {
        String TAG = getClass().getName() + "@registerLocationReceiver: ";

        Log.wtf(TAG, "" + "...");

        if(locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			/*if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Log.wtf(TAG, "registerLocationReceiver: " + "all location providers are ON");
			}
			else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Log.wtf(TAG, "registerLocationReceiver: " + "gps provider is ON");
			}
			else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Log.wtf(TAG, "registerLocationReceiver: " + "network provider is ON");
			}
			else {
				Log.wtf(TAG, "registerLocationReceiver: " + "all location providers are OFF");
			}/**/

            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            if(provider == null) provider = locationManager.getBestProvider(criteria, false);

            Location location = locationManager.getLastKnownLocation(provider);

            int div = sharedPref.getInt("serviceLevel", 1);
            if (div == 0) div = 1;
            locationManager.requestLocationUpdates(provider, Globals.task_latency/div, 100/div, this);/**/

            if (location != null) {
                Log.wtf(TAG, "" + provider + " provider selected.");
                //onLocationChanged(location);
				/*if(isWithinBorders((double) (location.getLatitude()), (double) (location.getLongitude()))) {
					Log.wtf(TAG, "registerLocationReceiver: " + "Inside borders.");
					//if(classToLoad == null) exeTask();
				}
				else {
					Log.wtf(TAG, "registerLocationReceiver: " + "Outside borders.");
				}/**/
            }
            else Log.wtf(TAG, "registerLocationReceiver: " + "Location not available.");
        }
    }

    public void unregisterLocationReceiver() {
        String TAG = getClass().getName() + "@unregisterLocationReceiver: ";

        Log.wtf(TAG, "" + "...");
        if(locationManager != null) {
            locationManager.removeUpdates(this);
        }
        locationManager = null;
    }

    /********** TIME **********/

    private boolean timeChanged(JSONObject prop, SharedPreferences pref) {
        try {
            if (!prop.isNull("timeFrom") && !prop.isNull("timeFrom")) {
                if (!prop.getString("timeFrom").equals(pref.getString("timeFrom", ""))) {
                    return true;
                }
                else if (!prop.getString("timeTo").equals(pref.getString("timeTo", ""))) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isWithinTime(String timeFrom, String timeTo) {
        Calendar calCurr = Calendar.getInstance();
		/*calCurr.set(Calendar.HOUR_OF_DAY, 21);
		calCurr.set(Calendar.MINUTE, 0);/**/
        calCurr.set(Calendar.SECOND, 0);

        Calendar calFrom = (Calendar) calCurr.clone();
        calFrom.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeFrom.split(":")[0]));
        calFrom.set(Calendar.MINUTE, Integer.parseInt(timeFrom.split(":")[1]));
        //calCurr.set(Calendar.SECOND, 1);

        Calendar calTo = (Calendar) calCurr.clone();
        calTo.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTo.split(":")[0]));
        calTo.set(Calendar.MINUTE, Integer.parseInt(timeTo.split(":")[1]));
        //calCurr.set(Calendar.SECOND, -1);

        if(calFrom.before(calTo)) {
            return calFrom.before(calCurr) && calTo.after(calCurr) || calFrom.equals(calCurr);
        }
        else if(calFrom.after(calTo)){
            return calFrom.before(calCurr) && calTo.before(calCurr)
                    || calFrom.after(calCurr) && calTo.after(calCurr)
                    || calFrom.equals(calCurr);
        }
        else return true;
    }

    public void onTimeChanged() {
        String TAG = getClass().getName() + "@onTimeChanged: ";

        String timeFrom = spGetString("timeFrom");
        String timeTo = spGetString("timeTo");
        Log.wtf(TAG, "" + "New time " + timeFrom + " - " + timeTo);
        if(classToLoad != null){
            Log.wtf(TAG, "" + "Already executing.");
            if(isWithinTime(timeFrom, timeTo)) {
                Log.wtf(TAG, "" + "In time.");
                setTimeTo(timeFrom, timeTo);
            }
            else {
                Log.wtf(TAG, "" + "Out of time.");
                stopExe();
                setTimeFrom(timeFrom, timeTo);
            }
        }
        else {
            Log.wtf(TAG, "" + "Not executing.");
            setTimeFrom(timeFrom, timeTo);
        }/**/
    }

    private void setTimeFrom(String timeFrom, String timeTo) {
        String TAG = getClass().getName() + "@setTimeFrom: ";

        Calendar calCurr = Calendar.getInstance();
		/*calCurr.set(Calendar.HOUR_OF_DAY, 0);
		calCurr.set(Calendar.MINUTE, 0);/**/
        calCurr.set(Calendar.SECOND, 0);


        Calendar calFrom = (Calendar) calCurr.clone();
        calFrom.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeFrom.split(":")[0]));
        calFrom.set(Calendar.MINUTE, Integer.parseInt(timeFrom.split(":")[1]));
        //calCurr.set(Calendar.SECOND, 1);

        Calendar calTo = (Calendar) calCurr.clone();
        calTo.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTo.split(":")[0]));
        calTo.set(Calendar.MINUTE, Integer.parseInt(timeTo.split(":")[1]));
        //calCurr.set(Calendar.SECOND, -1);

        Calendar cal = (Calendar) calFrom.clone();
        if(calFrom.before(calTo)) {
            if(calCurr.after(calTo) || calCurr.equals(calTo)) {
                cal.add(Calendar.DATE, 1);
            }
        }
        else if(calFrom.after(calTo)){
            if(calCurr.before(calTo) && calCurr.before(calFrom)) {
                cal.add(Calendar.DATE, -1);
            }
        }

        Log.wtf(TAG, "" + cal.getTime());

        Intent intent = new Intent(TaskService.this, TaskService.class);
        intent.putExtra("request", 1);
        PendingIntent pendingIntent = PendingIntent.getService(TaskService.this, 13, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);/**/
    }

    private void setTimeTo(String timeFrom, String timeTo) {
        String TAG = getClass().getName() + "@setTimeTo: ";

        Calendar calCurr = Calendar.getInstance();
		/*calCurr.set(Calendar.HOUR_OF_DAY, 21);
		calCurr.set(Calendar.MINUTE, 0);/**/
        calCurr.set(Calendar.SECOND, 0);

        Calendar calFrom = (Calendar) calCurr.clone();
        calFrom.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeFrom.split(":")[0]));
        calFrom.set(Calendar.MINUTE, Integer.parseInt(timeFrom.split(":")[1]));
        //calCurr.set(Calendar.SECOND, 1);

        Calendar calTo = (Calendar) calCurr.clone();
        calTo.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeTo.split(":")[0]));
        calTo.set(Calendar.MINUTE, Integer.parseInt(timeTo.split(":")[1]));
        //calCurr.set(Calendar.SECOND, -1);

        Calendar cal = (Calendar) calTo.clone();
        if(calFrom.equals(calTo)) {
            cal.add(Calendar.DATE, 1);
        }
        else if(calFrom.after(calTo)){
            if(calCurr.after(calFrom) && calCurr.after(calTo)) {
                cal.add(Calendar.DATE, 1);
            }
        }

        Log.wtf(TAG, "" + cal.getTime());

        Intent intent = new Intent(TaskService.this, TaskService.class);
        intent.putExtra("request", 0);
        PendingIntent pendingIntent = PendingIntent.getService(TaskService.this, 13, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);/**/
    }

	/*private void setTimeFrom(String time) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
		cal.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
		cal.set(Calendar.SECOND, 0);
		//cal.add(Calendar.SECOND, 10);

		Log.wtf(TAG, "setTimeFrom: " + cal.getTime());

		Intent intent = new Intent(TaskService.this, TaskService.class);
		intent.putExtra("request", 1);
		PendingIntent pendingIntent = PendingIntent.getService(TaskService.this, 13, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}/**/

	/*private void setTimeTo(String time) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
		cal.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
		cal.set(Calendar.SECOND, 0);

		Log.wtf(TAG, "setTimeTo: " + cal.getTime());

		Intent intent = new Intent(TaskService.this, TaskService.class);
		intent.putExtra("request", 0);
		PendingIntent pendingIntent = PendingIntent.getService(TaskService.this, 13, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}/**/

    private void cancelTime() {
        String TAG = getClass().getName() + "@cancelTime: ";

        Log.wtf(TAG, "" + "...");
        Intent intent = new Intent(TaskService.this, TaskService.class);
        PendingIntent pendingIntent = PendingIntent.getService(TaskService.this, 13, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    /********** MISC **********/

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            }
            catch (IOException ignore) {}
        }
    }

    public static void delete(File file) {
        if(file.exists()) {
            if(file.isDirectory()){
                if(file.list().length==0){
                    file.delete();
                }
                else {
                    String files[] = file.list();
                    for (String temp : files) {
                        File fileDelete = new File(file, temp);
                        delete(fileDelete);
                    }
                    if(file.list().length==0){
                        file.delete();
                    }
                }
            }
            else {
                file.delete();
            }
        }
    }

    public void taskFin() {
        String TAG = getClass().getName() + "@taskFin: ";

        Log.wtf(TAG, "" + "...");

        delete(new File(CLIENT + "/" + spGetString("taskID")));
        editor.putString("taskID", null);
        editor.putString("taskSize", null);
        editor.putString("className", null);
        editor.putString("dataName", null);
        editor.putString("dataChecked", "0");
        if (sharedPref.getBoolean("locStatus", false)) {
            editor.putBoolean("locStatus", false);
            editor.putString("locSWlat", null);
            editor.putString("locSWlng", null);
            editor.putString("locNElat", null);
            editor.putString("locNElng", null);
        }
        if (sharedPref.getBoolean("timeStatus", false)) {
            editor.putBoolean("timeStatus", false);
            editor.putString("timeFrom", null);
            editor.putString("timeTo", null);
        }
        editor.commit();


    }

    public String spGetString(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getString(key, "");
    }

    public void deletePendingData(String taskID) {
        String TAG = getClass().getName() + "@deletePendingData: ";

        Log.wtf(TAG, "" + "...");
        File file = returnFileFrom(CLIENT + "/" + taskID, ".dat.new");
        while (file != null) {
            delete(file);
            file = returnFileFrom(CLIENT + "/" + taskID, ".dat.new");
        }
        file = returnFileFrom(CLIENT + "/" + taskID, ".dat.new.part");
        while (file != null) {
            delete(file);
            file = returnFileFrom(CLIENT + "/" + taskID, ".dat.new.part");
        }
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String getParameter(String line, int element) {
        String parameter = "";
        int counter = 0;
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(line);
        while (m.find()) {
            if(element - counter == 1) {
                parameter = parameter + m.group(1);
                break;
            }
            else counter++;
        }
        return parameter;
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void mergeParts(InputStream input, String taskID) throws IOException {
        File file = new File(taskID);
        Long length = file.length();
        int read = 0;
        byte[] buffer = new byte[1024];
        RandomAccessFile output;
        output = new RandomAccessFile(file, "rw");
        output.seek(length);
        while((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(output);
        close(input);
    }

    public void mergeParts(String fileName, String partName) throws IOException {
        File file = new File(fileName);
        File part = new File(partName);
        Long length = file.length();
        InputStream input = new FileInputStream(partName);
        int read = 0;
        byte[] buffer = new byte[1024];
        RandomAccessFile output;
        output = new RandomAccessFile(file, "rw");
        output.seek(length);
        while((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(output);
        close(input);
        part.delete();
    }

    public File returnFileFrom(String url, String type) {
        File folder = new File(url);
        if(!folder.exists()) return null;
        String fileName;
        File file = null;
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(type)) {
                    file = new File(folder + "/" + fileName);
                    break;
                }
            }
        }
        return file;
    }

    public File returnPart(String url, long start) throws IOException {
        File file = new File(url);
        OutputStream output = new FileOutputStream(url + ".part");
        int read;
        byte[] buffer = new byte[1024];
        RandomAccessFile input;
        input = new RandomAccessFile(file, "r");
        input.seek(start);
        while ((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(output);
        close(input);
        File part = new File(url + ".part");
        return part;
    }

    public boolean searchStringFor(String string, String phrase) {
        int index = string.indexOf(phrase);
        return index >= 0;
    }

    private String writeToString(InputStream input) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String result = sb.toString();
        close(br);
        close(input);
        return result;
    }

    private ObjectOutputStream saveState(String taskID) throws IOException {
        String url = CLIENT + "/" + taskID + "/" + taskID + ".sav";
        if (new File(CLIENT + "/" + taskID).exists()) {
            FileOutputStream fos = new FileOutputStream(url);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            return oos;
        }
        else return null;
    }

    private void writeObjectToFile(Object object, String taskID) throws IOException {
        String ext = ".dat.new";
        String url = CLIENT + "/" + taskID + "/" + taskID;
        File file = new File(url + ext);
        int i = 1;
        while(file.exists() || new File(url + ".dat").exists()) {
            url = CLIENT + "/" + taskID + "/" + taskID + "(" + i + ")";
            file = new File(url + ext);
            i++;
        }
        FileOutputStream fos = new FileOutputStream(url + ext);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(object);

        close(oos);
        close(fos);
    }


    /*
     * Data integrity
     */

    boolean checkDataKeys(List data) {
        String TAG = getClass().getName() + "@checkDataKeys: ";

        for (Object o : data) {
            Map m = (Map) o;
            for (Object key : m.keySet()) {
                try {
                    if ("sensor".equals(key)
                            || "timestamp".equals(key)
                            || "values".equals(key)) {
                        Log.wtf(TAG, key + " key" + " OK");
                    } else {
                        Log.wtf(TAG, key + " key" + " ERROR");
                        return false;
                    }
                } catch (Exception e) {
                    Log.wtf(TAG, key + " key " + e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkDataValues(List data) {
        String TAG = getClass().getName() + "@checkDataValues: ";

        for (Object o : data) {
            Map m = (Map) o;
            for (Object key : m.keySet()) {
                if ("sensor".equals(key)) {
                    try {
                        int i = (int) m.get(key);
                        Log.wtf(TAG, key + " value " + "OK");
                    } catch (Exception e) {
                        Log.wtf(TAG, key + " value " + e.getMessage());
                        return false;
                    }
                } else if ("timestamp".equals(key)) {
                    try {
                        long l = (long) m.get(key);
                        Log.wtf(TAG, key + " value " + "OK");
                    } catch (Exception e) {
                        Log.wtf(TAG, key + " value " + e.getMessage());
                        return false;
                    }
                } else if ("values".equals(key)) {
                    try {
                        double[] d = (double[]) m.get(key);
                        Log.wtf(TAG, key + " value " + "OK");
                    } catch (Exception e) {
                        Log.wtf(TAG, key + " value " + e.getMessage());
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /*
     * Data I/O
     */
    private void saveDataObj(ArrayList<Object> data, String taskID) throws IOException {
        String ext = ".dat.new";
        String url = CLIENT + "/" + taskID + "/" + taskID;
        String deviceID = spGetString("deviceID");
        FileOutputStream fos;
        ObjectOutputStream oos;
        if(new File(url + ext).exists()) {
            fos = new FileOutputStream(url + ext, true);
            oos = new AppendingObjectOutputStream(fos);
        }
        else {
            fos = new FileOutputStream(url + ext);
            oos = new ObjectOutputStream(fos);
        }
        for(Object object : data) {
            oos.writeObject("[" + Calendar.getInstance().getTime() + "]" + " @ " + deviceID + ": " + object);
        }

        close(oos);
        close(fos);
    }

    private void saveDataOLD(ArrayList<String> data, String taskID) throws IOException {
        String ext = ".dat";
        String url = CLIENT + "/" + taskID + "/" + taskID;
        String deviceID = spGetString("deviceID");
        String dataName = taskID;
        File file = new File(url + ext);
		/*int i = 1;
		while(file.exists() || new File(url + ".dat").exists()) {
			dataName = taskID + "(" + i + ")";
			url = CLIENT + "/" + taskID + "/" + dataName;
			file = new File(url + ext);
			i++;
		}/**/
        FileWriter fw = null;
        if (file.exists()) fw = new FileWriter(file, true);
        else fw = new FileWriter(file);
        for(String string : data) {
            fw.write("[" + Calendar.getInstance().getTime() + "]" + " @ " + deviceID + ": " + string + "\n");
        }
        close(fw);
        //mergeData(taskID, dataName);
        //printData(url + ext);
    }

    private void mergeData(String taskID, String dataName) {
        String taskDir = CLIENT + "/" + taskID + "/";
        String dataUrl = taskDir + taskID + ".dat";
        String newDataUrl = taskDir + dataName + ".dat.new";
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            br = new BufferedReader(new FileReader(newDataUrl));
            fw = new FileWriter(new File(dataUrl), true);
            //fw.write(FileUtils.readFileToString(new File(newDataUrl)));
            String line;
            while ((line = br.readLine()) != null) {
                fw.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fw);
            close(br);
        }
    }

    private void printData(String url) {
        String TAG = getClass().getName() + "@printData: ";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(url));
            String line;
            while ((line = br.readLine()) != null) {
                Log.wtf(TAG, line);
                //Log.wtf(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(br);
        }
    }

    /*
     *
     */

    private void writeToFile(InputStream input, String url) throws IOException {
        OutputStream output = new FileOutputStream(new File(url));
        int read;
        byte[] buffer = new byte[1024];
        while((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(output);
        close(input);
    }

    private void writeToFile(String filePath, String string) throws IOException {
        File file = new File(filePath);
        if(!file.exists()) file.createNewFile();
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(string);
        //log = "";
        close(fileWriter);
        if(searchStringFor(string, "{EOL}")) {
            editor.putBoolean("logStatus", false);
            editor.commit();
        }
    }

    public static Context getContext() {
        return TaskService.getContext();
    }

    public void test() {
        String TAG = getClass().getName() + "@test: ";

        Log.wtf(TAG, "" + "...");

        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
    }

    public boolean taskIsNull(JSONObject task) {
        if(!task.isNull("taskID") && !task.isNull("taskSize") && !task.isNull("taskStatus") && !task.isNull("className") &&
                !task.isNull("locStatus") && !task.isNull("timeStatus")) {
            try {
                if(("on").equals(task.getString("locStatus"))) {
                    if(task.isNull("locSWlat") || task.isNull("locSWlng") || task.isNull("locNElat") || task.isNull("locNElng")){
                        return true;
                    }
                }
                if(("on").equals(task.getString("timeStatus"))) {
                    if(task.isNull("timeFrom") || task.isNull("timeTo")){
                        return true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        else {
            return true;
        }
    }
}
