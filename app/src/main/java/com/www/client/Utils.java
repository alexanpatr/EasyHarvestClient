package com.www.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";

    public static HttpResponse response(String url, String task) {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            if("get".equals(task)) {
                response = client.execute(new HttpGet(url));
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return response;
    }

    public static HttpResponse httpResponse(String url, ArrayList<NameValuePair> params) {
        Log.i(TAG, "response: " + params);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        try {
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(params));
            httpResponse = httpClient.execute(httppost);
        }
        catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return httpResponse;
    }


    public static File returnFileFrom(String url, String type) {
        String TAG = "Utils.returnFileFrom: ";

        Log.wtf(TAG, url + " with type " + type);

        File folder = new File(url);
        String fileName;
        File file = null;
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    fileName = listOfFiles[i].getName();
                    if (fileName.endsWith(type)) {
                        file = new File(url + "/" + fileName);
                        Log.wtf(TAG, "found " + fileName);
                        break;
                    }
                }
            }
        }
        return file;
    }

    public static void returnPart(RandomAccessFile input, OutputStream output, long start) throws IOException {
        int read;
        byte[] buffer = new byte[1024];
        input.seek(start);
        while ((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
        }
        close(output);
        close(input);
    }

    /**
     * Description: Read an input stream and covert it to readable string.
     * Parameters : - InputStream: The stream to convert to string.
     * Changelog  : - 150615: Remove throws clause and add appropriate log.
     */
    public static String writeToString(InputStream input) {
        String TAG = Utils.class.getName() + "@writeToString: ";
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        String result = sb.toString();
        close(br);
        close(isr);
        close(input);
        return result;
    }

    public static void writeToFile(InputStream input, String url) {
        File file = new File(url);
        long seek = file.length();
        RandomAccessFile output = null;
		/*long skip = 0;
		skip += input.skip(seek);
		Log.i(TAG, "writeToFile: before " + skip + " - " + seek);
		while(skip < seek) {
			skip += input.skip(1);
		}
		Log.i(TAG, "writeToFile: after " + skip);/**/
        try {
            output = new RandomAccessFile(file, "rw");
            output.seek(seek);
            int read = 0;
            byte[] buffer = new byte[1024];
            while((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        close(output);
        close(input);/**/
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

    public static void writeJSONtoFile(JSONObject json, File file) {
        FileWriter fw = null;
        try {
            if (file.exists()) {
                fw = new FileWriter(file, true);
                //json.put("id", countLines(file));
            }
            else {
                fw = new FileWriter(file);
                //json.put("id", 0);
            }
            fw.write(json.toString() + "\n");
            close(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("json: " + json);
        //System.out.println("string: " + json.toString());
        //System.out.println("file: " + FileUtils.readFileToString(file));
    }

    public static void overwriteJSONtoFile(ArrayList<JSONObject> arrayList, File file) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            for (JSONObject jsonObject : arrayList) {
                fw.write(jsonObject.toString() + "\n");
            }
            close(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countLines(File file) {
        int lines = 0;
        try {
            lines = countLines(FileUtils.readFileToString(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    public static ArrayList<JSONObject> fileToJSON(File file) {
        ArrayList<JSONObject> arrayList = new ArrayList<JSONObject>();
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    //System.out.println("line: " + line);
                    arrayList.add(new JSONObject(line));
                }
                close(br);
                //System.out.println("array: " + arrayList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    public static void removePM(Context c, int id) {
        String TAG = Utils.class.getName() + "@removePM: ";

        Log.wtf(TAG, "Removing PM " + id + "...");

        delete(new File(Globals.pms_dir + "/" + id));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor ed = sp.edit();

        ed.putString(Globals.pm_id, null);
        ed.putString(Globals.pm_name, null);
        ed.putString(Globals.pm_vers, null);
//
//        class name
        ed.putString(Globals.pm_clss, null);
//
//        sensing task id
        ed.putString(Globals.pm_st_id, null);
//
        ed.putString(Globals.pm_desc, null);
        ed.putString(Globals.pm_user, null);
        ed.putString(Globals.pm_date, null);
        ed.putString(Globals.pm_time, null);
        ed.putString(Globals.pm_size, null);

        ed.commit();
    }

    public static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
            }
        }
    }











    // Data related functions
    //
    //
    public static boolean saveData(List data, String taskID) {
        String TAG = "Utils@saveData: ";

        String ext = ".dat";
        String url = Globals.client_dir + "/" + taskID + "/" + taskID;

        int i = 1;
        File file = new File(url + ext);
//        while(file.exists() || new File(url + ".dat").exists()) {
        // TODO: EVAL
        /*
        if (file.exists() || new File(url + ".dat").exists()) {
            url = Globals.client_dir + "/" + taskID + "/" + taskID + "(" + i + ")";
            file = new File(url + ext);
            i++;
        }
        */
        // /TODO: EVAL
        url += ext;

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            Log.wtf(TAG, "Saving data to " + url);
            List<Object> oldData = Utils.getData(url);
            if (!oldData.isEmpty()) {
                oldData.addAll(data);
                data = oldData;
            }
            fos = new FileOutputStream(url);
            oos = new ObjectOutputStream(fos);

            oos.writeObject(data);

            close(oos);
            close(fos);

        } catch (Exception ex) {
            Log.wtf(TAG, ex.getMessage());
            return false;
        }
        Log.wtf(TAG, "OK");
        return true;
    }

    //
    public static void mergeData(String dataSrc, String dataDst) {
        String TAG = "Utils@mergeData: ";

        if(new File(dataDst).exists()) {
            Log.wtf(TAG, "Merging data with " + dataDst);

            List<Object> oldData = Utils.getData(dataDst);
            List<Object> newData = Utils.getData(dataSrc);
            oldData.addAll(newData);

            new File(dataSrc).delete();
            new File(dataDst).delete();

            try {
                FileOutputStream fos = new FileOutputStream(dataDst);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(oldData);

                close(oos);
                close(fos);
            } catch (Exception ex) {
                Log.wtf(TAG, ex.getMessage());
            }
        } else {
            Log.wtf(TAG, "Moving data to " + dataDst);
            new File(dataSrc).renameTo(new File(dataDst));
            new File(dataSrc).delete();
        }

    }

    //
    public static List<Object> getData(String url) {
        String TAG = "Utils@getData: ";

        FileInputStream fis;
        ObjectInputStream ois;

        List<Object> data = new ArrayList<>();

        try {
            Log.wtf(TAG, "Returning data " + url);
            if (new File(url).exists()) {
                fis = new FileInputStream(url);
                ois = new ObjectInputStream(fis);

                data = (List<Object>) ois.readObject();

                close(ois);
                close(fis);
            } else {
                Log.wtf(TAG, "File does not exist");
            }
        } catch (Exception ex) {
            Log.wtf(TAG, ex.getMessage());
        }
        Log.wtf(TAG, "OK");
        return data;
    }

    public static ObjectInputStream getData(List<Object> data) {
        String TAG = "Utils@getDataFrom: ";

        Log.wtf(TAG, "...");

        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(data);

            Utils.close(oos);

            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(is);

        } catch (IOException e) {
            Log.wtf(TAG, e.getMessage());
        }
        return ois;
    }

    //
    public static ObjectOutputStream putData (String url) {
        String TAG = "Utils@putDataTo: ";

        Log.wtf(TAG, "...");

        FileOutputStream fos;
        ObjectOutputStream oos = null;

        try {
//            Log.wtf(TAG, "Saving data to " + url);

            fos = new FileOutputStream(url);
            oos = new ObjectOutputStream(fos);
        } catch (Exception ex) {
            Log.wtf(TAG, ex.getMessage());
        }
        return oos;
    }

    // TODO
    public static String getNewDataPath (int stId) {
        String TAG = "Utils@getNewDataPath: ";

        String ext = ".dat";
        String url = Globals.client_dir + "/" + stId + "/" + stId;

        int i = 1;
        File file = new File(url + ext);
        while(file.exists() || new File(url + ".dat").exists()) {
            url = Globals.client_dir + "/" + stId + "/" + stId + "(" + i + ")";
            file = new File(url + ext);
            i++;
        }

        file = new File(url + ext + ".tmp");
        while(file.exists() || new File(url + ".dat" + ".tmp").exists()) {
            url = Globals.client_dir + "/" + stId + "/" + stId + "(" + i + ")";
            file = new File(url + ext);
            i++;
        }

        url += ext + ".tmp";

        return url;
    }



    // State related functions
    //
    // getStateFrom the pm directory
    public static ObjectInputStream getStateFrom (int id) {
        final String TAG = "Utils@getStateFrom: ";

        FileInputStream fis;
        ObjectInputStream ois = null;

        String url = Globals.pms_dir + "/" + id + "/" + id + ".sav";

        Log.wtf(TAG, "" + url);

        if(new File(url).exists()) {
            Log.wtf(TAG, "" + "OK");
            try {
                fis = new FileInputStream(url);
                ois = new ObjectInputStream(fis);
            } catch (Exception e) {
                Log.wtf(TAG, e.getMessage());
            }
        } else {
            Log.wtf(TAG, "No saved state found");
        }
        return ois;
    }

    // putStateTo the pm directory
    public static ObjectOutputStream putStateTo (String url) {
        String TAG = "Utils@putStateTo: ";

        Log.wtf(TAG, url);

        FileOutputStream fos;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(url);
            oos = new ObjectOutputStream(fos);
        } catch (Exception ex) {
            Log.wtf(TAG, ex.getMessage());
        }
        return oos;
    }




}
